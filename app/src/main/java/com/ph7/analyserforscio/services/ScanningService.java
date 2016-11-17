package com.ph7.analyserforscio.services;


import android.util.Log;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.analyserforscio.application.FoodScanApplication;
import com.ph7.analyserforscio.callbacks.ScanHandler;
import com.ph7.analyserforscio.callbacks.ScanUpdateHandler;
import com.ph7.analyserforscio.callbacks.ScioDeviceScanHandler;
import com.ph7.analyserforscio.models.ph7.ScioReadingWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by craigtweedy on 16/06/2016.
 */
public class ScanningService {

    ScanUpdateHandler updateHandler;

    enum ScanCompleteStatus {
        Success,
        Error
    }

    private interface ScanCompleteHandler {
        void onComplete(ScanCompleteStatus status, ScanRunner runner);
    }

    String TAG = "ScanningService";
    int NUMBER_OF_SCANS = 1;
    int CURRENT_SCAN_ITERATION = 0;
    List<ScioReadingWrapper> scioReadings = new ArrayList<>();
    ScanRunner currentScan;
    ScanHandler scanHandler;

    public void execute(ScanHandler scanHandler, int num_of_scans, ScanUpdateHandler updateHandler) {
        if (scanHandler == null) {
            Log.e(TAG, "Scan handler must be set");
            return;
        }
        this.NUMBER_OF_SCANS = num_of_scans ;
        this.scanHandler = scanHandler;
        this.updateHandler = updateHandler;
        startScanning();
    }

    private void startScanning() {
        nextScan();
    }

    private void nextScan() {
        this.updateHandler.updateScanCount(NUMBER_OF_SCANS-CURRENT_SCAN_ITERATION);
        final ScanningService _this = this;
        currentScan = new ScanRunner();
        currentScan.run(new ScanCompleteHandler() {
            @Override
            public void onComplete(ScanCompleteStatus status, ScanRunner runner) {
                if (status == ScanCompleteStatus.Error) {
                    //Stop all
                    scanningFailed(runner.getError());
                } else {
                    CURRENT_SCAN_ITERATION++;
                    scioReadings.add(new ScioReadingWrapper(new Date(), runner.getReading()));
                    if (CURRENT_SCAN_ITERATION < NUMBER_OF_SCANS) {
                        new Timer().schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {

                                        nextScan();
                                    }
                                },
                                2000
                        );
                    } else {
                        stopScanning();
                    }
                }
            }
        });
    }
    void scanningFailed(Error error) {
        this.scanHandler.onFailed(error);
    }

    void stopScanning() {
        this.scanHandler.onComplete(this.scioReadings);
    }

    private class ScanRunner {

        private ScioReading scioReading;
        private Error error;

        public ScioReading getReading() {
            return this.scioReading;
        }

        public Error getError() {
            return this.error;
        }

        private void run(final ScanCompleteHandler callback) {

            final ScanRunner _this = this;
            FoodScanApplication.getDeviceHandler().scan(new ScioDeviceScanHandler() {
                @Override
                public void onSuccess(ScioReading scioReading) {
                    _this.scioReading = scioReading;
                    callback.onComplete(ScanCompleteStatus.Success, _this);
                }

                @Override
                public void onNeedCalibrate() {
                    _this.error = new DeviceNeedsCalibrationError("Device needs calibration");
                    callback.onComplete(ScanCompleteStatus.Error, _this);
                }

                @Override
                public void onError() {
                    _this.error = new ScanError("Something went wrong");
                    callback.onComplete(ScanCompleteStatus.Error, _this);
                }

                @Override
                public void onTimeout() {
                    _this.error = new ScanTimeoutError("Scanning timed out");
                    callback.onComplete(ScanCompleteStatus.Error, _this);
                }
            });
        }
    }

    public class DeviceNeedsCalibrationError extends Error {
        DeviceNeedsCalibrationError(String msg) {
            super(msg);
        }
    }
    public class ScanError extends Error {
        ScanError(String msg) {
            super(msg);
        }
    }
    public class ScanTimeoutError extends Error {
        ScanTimeoutError(String msg) {
            super(msg);
        }
    }
}


