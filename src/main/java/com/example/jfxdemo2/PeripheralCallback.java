package com.example.jfxdemo2;

import com.welie.blessed.*;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static com.example.jfxdemo2.SensorTagConstants.*;
import static com.example.jfxdemo2.SensorTagDataProcessor.calculateTemperature;
import static com.welie.blessed.BluetoothCommandStatus.COMMAND_SUCCESS;

public class PeripheralCallback extends BluetoothPeripheralCallback {

    private final StringProperty irTemperatureString;
    private final StringProperty ambientTemperatureString;

    public PeripheralCallback(StringProperty irTemperatureString, StringProperty ambientTemperatureString) {
        this.irTemperatureString = irTemperatureString;
        this.ambientTemperatureString = ambientTemperatureString;
    }

    @Override
    public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral,
                                     @NotNull List<BluetoothGattService> services) {
        if (peripheral.getService(UUID_TEMPERATURE_SERVICE) != null) {
            peripheral.writeCharacteristic(UUID_TEMPERATURE_SERVICE,
                    UUID_TEMPERATURE_CONFIG,
                    ENABLE_COMMAND,
                    BluetoothGattCharacteristic.WriteType.WITH_RESPONSE);
            BluetoothGattCharacteristic temperatureDataChar = peripheral.getCharacteristic(
                    UUID_TEMPERATURE_SERVICE,
                    UUID_TEMPERATURE_DATA);
            peripheral.setNotify(temperatureDataChar, true);

        }

    }

    @Override
    public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral,
                                      byte[] value,
                                      @NotNull BluetoothGattCharacteristic characteristicUUID,
                                      @NotNull BluetoothCommandStatus status) {

        // Deal with errors
        if (status != COMMAND_SUCCESS) {
            //log.error("command failed with status {}", status);
            return;
        }

        if (characteristicUUID.getUuid().equals(UUID_TEMPERATURE_CONFIG)) {
            //log.info("temperature notifications configured");

            BluetoothGattCharacteristic temperatureDataChar = peripheral.getCharacteristic(
                    UUID_TEMPERATURE_SERVICE,
                    UUID_TEMPERATURE_DATA);
            peripheral.setNotify(temperatureDataChar, true);

        }

    }

    @Override
    public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral,
                                       byte[] value,
                                       @NotNull BluetoothGattCharacteristic characteristic,
                                       @NotNull BluetoothCommandStatus status) {
        final UUID characteristicUUID = characteristic.getUuid();

        // Deal with errors
        if (status != COMMAND_SUCCESS) {
            //log.error("command failed with status {}", status);
            return;
        }

        if (characteristicUUID.equals((UUID_TEMPERATURE_DATA))) {
            //log.info("receiving temperature data update");

            var result = calculateTemperature(value);

            if (result.length >= 2) {

                Platform.runLater(() -> {
                    irTemperatureString.setValue(String.format("%.2f", result[0]));
                    ambientTemperatureString.setValue(String.format("%.2f", result[1]));

                });
            }
        }
    }
}