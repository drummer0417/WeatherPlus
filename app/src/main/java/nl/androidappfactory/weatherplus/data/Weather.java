package nl.androidappfactory.weatherplus.data;

import java.sql.Time;

/**
 * Created by HA256157 on 29/10/2015.
 */
public class Weather {

    private String currentTemp;
    private String minTemp;
    private String maxTemp;
    private String windSpeed;
    private String windDeg;
    private String humidity;
    private String pressure;
    private String description;
    private String icon;
    private String sunSet;
    private String sunRise;
    private String country;
    private String city;

    @Override
    public String toString() {
        return "Weather{" +
                "currentTemp='" + currentTemp + '\'' +
                ", minTemp='" + minTemp + '\'' +
                ", maxTemp='" + maxTemp + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", windDeg='" + windDeg + '\'' +
                ", humidity='" + humidity + '\'' +
                ", pressure='" + pressure + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", sunSet='" + sunSet + '\'' +
                ", sunRise='" + sunRise + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

    public String getPressure() {
        if (pressure == null)
            return "-";
        else
            return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getCurrentTemp() {
        if (currentTemp == null)
            return "-";
        else
            return currentTemp;
    }

    public String getCity() {
        if (city == null)
            return "-";
        else
            return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCurrentTemp(String currentTemp) {
        this.currentTemp = currentTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getCountry() {
        if (country == null)
            return "-";
        else
            return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMaxTemp() {
        if (maxTemp == null)
            return "-";
        else
            return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getWindSpeed() {
        if (windSpeed == null)
            return "-";
        else
            return windSpeed;
    }

    public String getWindSpeedBeuafort() {
        String beaufort = null;
        if (windSpeed == null)
            return "-";
        else {
            double iWindSpeed = Double.parseDouble(windSpeed);
            if (iWindSpeed < 0.3) {
                beaufort = "0";
            } else if (iWindSpeed < 1.6) {
                beaufort = "1";
            } else if (iWindSpeed < 3.4) {
                beaufort = "2";
            } else if (iWindSpeed < 5.5) {
                beaufort = "3";
            } else if (iWindSpeed < 8.0) {
                beaufort = "4";
            } else if (iWindSpeed < 10.8) {
                beaufort = "5";
            } else if (iWindSpeed < 13.7) {
                beaufort = "6";
            } else if (iWindSpeed < 17.2) {
                beaufort = "7";
            } else if (iWindSpeed < 20.8) {
                beaufort = "8";
            } else if (iWindSpeed < 24.5) {
                beaufort = "9";
            } else if (iWindSpeed < 28.5) {
                beaufort = "10";
            } else if (iWindSpeed < 32.6) {
                beaufort = "11";
            } else {
                beaufort = "12";
            }
            return beaufort;
        }

    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDeg() {
        if (windDeg == null)
            return "-";
        else
            return windDeg;
    }

    public String getWindDirections() {

        if (windDeg == null)
            return "-";
        else {

            String direction = null;
            double dDeg = Double.parseDouble(windDeg);
            if ((dDeg >= 348.75 && dDeg < 360) || (dDeg >= 0 && dDeg < 11.25)) {
                direction = "N";
            } else if ((dDeg < 33.75)) {
                direction = "NNO";
            } else if ((dDeg < 56.25)) {
                direction = "NO";
            } else if ((dDeg < 78.75)) {
                direction = "ONO";
            } else if ((dDeg < 101.25)) {
                direction = "O";
            } else if ((dDeg < 123.75)) {
                direction = "OZO";
            } else if ((dDeg < 146.25)) {
                direction = "ZO";
            } else if ((dDeg < 168.75)) {
                direction = "ZZO";
            } else if ((dDeg < 191.25)) {
                direction = "Z";
            } else if ((dDeg < 213.75)) {
                direction = "ZZW";
            } else if ((dDeg < 236.25)) {
                direction = "ZW";
            } else if ((dDeg < 258.75)) {
                direction = "WZW";
            } else if ((dDeg < 281.25)) {
                direction = "W";
            } else if ((dDeg < 303.75)) {
                direction = "WNW";
            } else if ((dDeg < 348.75)) {
                direction = "NNW";
            } else {
                direction = "Unknown direction";
            }
            return direction;
        }
    }

    public void setWindDeg(String windDeg) {
        this.windDeg = windDeg;
    }

    public String getHumidity() {
        if (humidity == null)
            return "-";
        else
            return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        if (description == null)
            return "-";
        else
            return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSunSet() {
        return sunSet;
    }

    public String getSunSetTime() {
        if (sunSet == null)
            return "-";
        else {
            Time sunsetTime = new Time(Long.parseLong(sunSet));
            return sunsetTime.toString();
        }
    }

    public String getSunRiseTime() {
        if (sunRise == null)
            return "-";
        else {
            Time sunRiseTime = new Time(Long.parseLong(sunRise));
            return sunRiseTime.toString();
        }
    }

    public void setSunSet(String sunSet) {
        this.sunSet = sunSet;
    }

    public String getSunRise() {
        if (sunRise == null)
            return "-";
        else
            return sunRise;
    }

    public void setSunRise(String sunRise) {
        this.sunRise = sunRise;
    }
}