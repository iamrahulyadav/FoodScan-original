package com.ph7.foodscan.models.ph7;

import android.os.Parcel;
import android.os.Parcelable;

import com.consumerphysics.android.sdk.model.ScioReading;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Created by craigtweedy on 12/07/2016.
 */
public class ScioReadingWrapper implements Parcelable {

    Date scannedAt;
    ScioReading scioReading;

    public ScioReadingWrapper (Date scannedAt, ScioReading scioReading) {
        this.scannedAt = scannedAt;
        this.scioReading = scioReading;

    }

    public Date getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Date scannedAt) {
        this.scannedAt = scannedAt;
    }

    public ScioReading getScioReading() {
        return scioReading;
    }

    public void setScioReading(ScioReading scioReading) {
        this.scioReading = scioReading;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] reading = toByteArray(scioReading);
        parcel.writeInt(reading.length);
        parcel.writeByteArray(reading);
        parcel.writeSerializable(scannedAt);
    }

    private ScioReadingWrapper(Parcel in) {
        byte[] reading = new byte[in.readInt()];
        in.readByteArray(reading);
        scioReading = fromByteArray(reading);
        scannedAt = (Date)in.readSerializable();
    }

    public static final Creator<ScioReadingWrapper> CREATOR = new Creator<ScioReadingWrapper>() {
        @Override
        public ScioReadingWrapper createFromParcel(Parcel in) {
            return new ScioReadingWrapper(in);
        }

        @Override
        public ScioReadingWrapper[] newArray(int size) {
            return new ScioReadingWrapper[size];
        }
    };

    private byte[] toByteArray(ScioReading reading) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(reading);
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } catch (IOException ex) {

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    private ScioReading fromByteArray(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            ScioReading reading = (ScioReading) in.readObject();
            return reading;
        } catch (IOException ex) {

        } catch (ClassNotFoundException ex) {

        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }
}
