package lamborghini.wallpapers.CarWallpapers.CarSounds.utils;

public class AnalyticLogs {

    public static int getNumSeg(int iTmp){

        if (iTmp >= 500){
            return 500;
        }else if(iTmp >= 400){
            return 400;
        }else if(iTmp >= 300){
            return 300;
        }else if(iTmp >= 200){
            return 200;
        }else if(iTmp >= 100){
            return 100;
        }else if(iTmp >= 50){
            return 50;
        }else if(iTmp >= 40){
            return 40;
        }else if(iTmp >= 30){
            return 30;
        }else if(iTmp >= 20){
            return 20;
        }else if(iTmp >= 10){
            return 10;
        }else if(iTmp >= 5){
            return 5;
        }else if(iTmp == 4){
            return 4;
        }else if (iTmp == 3){
            return 3;
        }else if(iTmp == 2){
            return 2;
        }else if(iTmp == 1){
            return 1;
        }else{
            return 0;
        }

    }

}
