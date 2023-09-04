package com.example.whatstheweather.utils;

public class Formats {

    public String dateFormat(String date){
        date = date.substring(8,10) + "/" + date.substring(5,7);
        return date;
    }
    public String hourFormat(String hour){
        hour = hour.substring(11);
        return hour;
    }

    public double kphToMs(double kph){
        return Math.ceil(100*kph*1000/3600)/100;
    }

    public String windDir(String wind_dir){
        switch (wind_dir){
            case "N":
                wind_dir = "North";
                break;
            case "S":
                wind_dir = "South";
                break;
            case "E":
                wind_dir = "East";
                break;
            case "W":
                wind_dir = "West";
                break;
            case "SW":
                wind_dir = "SouthWest";
                break;
            case "SE":
                wind_dir = "SouthEast";
                break;
            case "NW":
                wind_dir = "NorthWest";
                break;
            case "NE":
                wind_dir = "NorthEast";
                break;
            case "NNW":
                wind_dir = "NorthNorthWest";
                break;
            case "WNW":
                wind_dir = "WestNorthWest";
                break;
            case "WSW":
                wind_dir = "WestSouthWest";
                break;
            case "SSW":
                wind_dir = "SouthSouthWest";
                break;
            case "SSE":
                wind_dir = "SouthSouthEast";
                break;
            case "ESE":
                wind_dir = "EastSouthEast";
                break;
            case "ENE":
                wind_dir = "EastNorthEast";
                break;
            case "NNE":
                wind_dir = "NorthNorthEast";
                break;
            default:
                wind_dir += " (rajouter celui là ;)";
                break;
        }
        return wind_dir;
    }

}
