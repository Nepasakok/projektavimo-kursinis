package autodviratis.web;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;

import java.math.BigDecimal;

final class Labels {

    static final String NOT_SPECIFIED = "nenurodyta";

    private Labels() {
    }

    static String type(BikeType type) {
        return switch (type) {
            case MOUNTAIN -> "kalnų";
            case ROAD -> "plento";
            case CITY -> "miesto";
            case KIDS -> "vaikiškas";
            case NOT_SPECIFIED -> NOT_SPECIFIED;
        };
    }

    static String brake(BrakeType brakeType) {
        return switch (brakeType) {
            case DISC -> "diskiniai";
            case HYDRAULIC -> "hidrauliniai";
            case V_BRAKE -> "V-brake";
            case NOT_SPECIFIED -> NOT_SPECIFIED;
        };
    }

    static String material(FrameMaterial material) {
        return switch (material) {
            case ALUMINIUM -> "aliuminis";
            case STEEL -> "plienas";
            case CARBON -> "karbonas";
            case NOT_SPECIFIED -> NOT_SPECIFIED;
        };
    }

    static String wheelSize(double wheelSize) {
        if (wheelSize == Listing.WHEEL_SIZE_NOT_SPECIFIED) {
            return NOT_SPECIFIED;
        }
        String number = BigDecimal.valueOf(wheelSize).stripTrailingZeros().toPlainString();
        return number.replace('.', ',') + "\"";
    }

    static String frameSize(String frameSize) {
        if (frameSize.equals(Listing.FRAME_SIZE_NOT_SPECIFIED)) {
            return NOT_SPECIFIED;
        }
        return frameSize;
    }

    static String gears(int gears) {
        if (gears == Listing.GEARS_NOT_SPECIFIED) {
            return NOT_SPECIFIED;
        }
        return String.valueOf(gears);
    }

    static String price(BigDecimal price) {
        return price.stripTrailingZeros().toPlainString() + " €";
    }
}
