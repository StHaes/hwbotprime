package org.hwbot.prime.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.hwbot.api.bench.dto.DeviceInfoDTO;
import org.hwbot.prime.api.HardwareStatusAware;
import org.hwbot.prime.api.NetworkStatusAware;
import org.hwbot.prime.service.BenchService;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

public class HardwareDetectionTask extends AsyncTask<String, Void, DeviceInfoDTO> {

    public static String deviceName;
    public static int maxAttempts = 10;
    private HardwareStatusAware observer;
    private final NetworkStatusAware networkStatusAware;
    private final boolean strict = true;

    public HardwareDetectionTask(NetworkStatusAware networkStatusAware, HardwareStatusAware observer) {
        this.networkStatusAware = networkStatusAware;
        this.observer = observer;
    }

    @Override
    public DeviceInfoDTO doInBackground(String... param) {
        if (param[0].equals(deviceName) && param.length == 1) {
            Log.e(this.getClass().getSimpleName(), "Aborting, already loaded device info for " + param[0]);
            return null;
        } else if (maxAttempts-- <= 0) {
            Log.w(this.getClass().getSimpleName(), "Aborting, max attempts to load device info for " + param[0]);
        }
        String description = null;
        deviceName = param[0];
        if (param.length > 1){
            description = param[1];
        }
        Reader reader = null;
        try {
            URL url = new URL(BenchService.SERVER + "/api/hardware/device/info?name=" + URLEncoder.encode(deviceName, "utf8") +"&strict="+strict+ (description != null ? "&description="+URLEncoder.encode(description, "utf8") : ""));
            // Log.i(this.getClass().getSimpleName(), "Loading device info from: " + url);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            DeviceInfoDTO deviceInfoWithRecordsDTO = new Gson().fromJson(reader, DeviceInfoDTO.class);
            // Log.i(this.getClass().getSimpleName(), "Got device: " + deviceInfoWithRecordsDTO);
            if (deviceInfoWithRecordsDTO == null || deviceInfoWithRecordsDTO.getId() == null) {
                observer.notifyDeviceInfoFailed(org.hwbot.prime.api.HardwareStatusAware.Status.unknown_device);
            } else {
                observer.notifyDeviceInfo(deviceInfoWithRecordsDTO);
            }
        } catch (UnknownHostException e) {
            Log.w(this.getClass().getSimpleName(), "No network access: " + e.getMessage());
            networkStatusAware.showNetworkPopupOnce();
            observer.notifyDeviceInfoFailed(org.hwbot.prime.api.HardwareStatusAware.Status.no_network);
        } catch (Exception e) {
            Log.i(this.getClass().getSimpleName(), "Error: " + e.getMessage());
            e.printStackTrace();
            observer.notifyDeviceInfoFailed(org.hwbot.prime.api.HardwareStatusAware.Status.service_down);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}