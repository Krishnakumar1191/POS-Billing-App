package com.example.posapp.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.posapp.data.entities.Order
import com.example.posapp.data.entities.OrderLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

class PrinterHelper(private val context: Context) {

    // Standard SPP UUID for Bluetooth Serial Port Profile (Printers)
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    suspend fun printReceipt(
        order: Order, 
        items: List<OrderLine>, 
        method: String, 
        shopName: String, 
        width: Int = 58, // 58mm for mobile Bluetooth, 80mm for thermal e-POS
        isWifi: Boolean = false, 
        address: String = ""
    ) {
        withContext(Dispatchers.IO) {
            try {
                val receiptText = buildReceiptString(order, items, method, shopName, width)
                if (isWifi) {
                    printViaWifi(address, receiptText, width)
                } else {
                    printViaBluetooth(address, receiptText, width)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Printer Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun buildReceiptString(order: Order, items: List<OrderLine>, paymentMethod: String, shopName: String, width: Int): String {
        // 58mm printer fits ~32 characters, 80mm e-POS printer fits ~48 characters per line
        val lineWidth = if (width == 80) 48 else 32
        val divider = "-".repeat(lineWidth)

        val builder = StringBuilder()
        // Standard ESC/POS Init
        builder.append(27.toChar()).append('@') 
        builder.append("$divider\n")
        
        // Center shop name dynamically
        val title = shopName.take(lineWidth)
        val padding = (lineWidth - title.length) / 2
        val centeredTitle = " ".repeat(maxOf(0, padding)) + title
        builder.append("$centeredTitle\n")
        
        builder.append("$divider\n")
        builder.append("Order: ${order.orderNumber}\n")
        
        if (!order.customerName.isNullOrBlank()) {
            builder.append("Customer: ${order.customerName}\n")
        }
        if (!order.customerPhone.isNullOrBlank()) {
            builder.append("Phone: ${order.customerPhone}\n")
        }
        
        builder.append("$divider\n")
        
        items.forEach { line ->
            builder.append("Item #${line.productId} x ${line.quantity.toInt()}   Rs${"%.2f".format(line.lineTotal)}\n")
        }
        
        builder.append("$divider\n")
        builder.append("TOTAL: Rs${"%.2f".format(order.totalAmount)}\n")
        builder.append("Payment: $paymentMethod\n")
        builder.append("$divider\n")
        builder.append("      Thank you for visiting!   \n")
        builder.append("\n\n\n") // Feed paper
        return builder.toString()
    }

    private fun printViaBluetooth(macAddress: String, receiptText: String, width: Int) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            throw Exception("Bluetooth is disabled or not supported")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                throw Exception("Missing BLUETOOTH_CONNECT permission")
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                throw Exception("Missing BLUETOOTH permission")
            }
        }

        val canCancelDiscovery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
        if (canCancelDiscovery && bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        val printerDevice = if (macAddress.isNotBlank()) {
            try {
                bluetoothAdapter.getRemoteDevice(macAddress)
            } catch (_: IllegalArgumentException) {
                throw Exception("Invalid Bluetooth MAC Address")
            }
        } else {
            val pairedDevices = bluetoothAdapter.bondedDevices
            pairedDevices.firstOrNull { it.bluetoothClass.majorDeviceClass == 1536 || it.name?.contains("Printer", ignoreCase = true) == true } 
                ?: pairedDevices.firstOrNull() 
                ?: throw Exception("No paired Bluetooth printer found")
        }

        val socket = try {
            printerDevice.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (e: Exception) {
            throw Exception("Socket creation failed: ${e.message}")
        }
        
        try {
            socket.connect()
        } catch (e: Exception) {
            try { socket.close() } catch (_: Exception) { }
            try {
                val fallbackSocket = printerDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType).invoke(printerDevice, 1) as BluetoothSocket
                fallbackSocket.connect()
                val outputStream: OutputStream = fallbackSocket.outputStream
                outputStream.write(receiptText.toByteArray())
                if (width == 80) {
                    val cutCommand = byteArrayOf(0x1D, 0x56, 0x41, 0x00)
                    outputStream.write(cutCommand)
                }
                outputStream.flush()
                fallbackSocket.close()
                return
            } catch (_: Exception) {
                throw Exception("Printer connection failed. Ensure printer is on and paired.")
            }
        }

        val outputStream: OutputStream = socket.outputStream
        outputStream.write(receiptText.toByteArray())
        if (width == 80) {
            val cutCommand = byteArrayOf(0x1D, 0x56, 0x41, 0x00)
            outputStream.write(cutCommand)
        }
        outputStream.flush()
        socket.close()
    }

    private fun printViaWifi(ipAddress: String, receiptText: String, width: Int) {
        if (ipAddress.isBlank()) throw Exception("Wi-Fi IP Address is empty")
        val port = 9100 // Default raw printing port for receipt printers
        
        // Establish socket connection with a 4-second timeout to prevent infinite hanging
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(ipAddress.trim(), port), 4000)
        } catch (e: Exception) {
            throw Exception("Could not connect to printer at $ipAddress:9100. Check IP and Wi-Fi connection.")
        }

        val outputStream = socket.getOutputStream()
        outputStream.write(receiptText.toByteArray())
        
        // If 80mm e-POS thermal printer, send ESC/POS paper cut command (GS V A 0)
        if (width == 80) {
            val cutCommand = byteArrayOf(0x1D, 0x56, 0x41, 0x00)
            outputStream.write(cutCommand)
        }
        
        outputStream.flush()
        socket.close()
    }
}