import java.util.Scanner;
class Video{
    private String title;
    private boolean isChecked;
    private float rating;
    public Video(String title){
        this.title=title;
        this.isChecked=false;
        this.rating=0;
    }
    public String getTitle(){
        return title;
    }
    public boolean isCheckedOut(){
        return isChecked;
    }
    public float getRating(){
        return rating;
    }
    public void returnVideo(){
        this.isChecked=false;
    }
    public void checkOut(){
        this.isChecked=true;
    }
    public void giveRating(float rating){
        this.rating=rating;
    }
}
class VideoStore{
    private int size;
    private Video[] list;
    public VideoStore(){
        list=new Video[10];
        size=0;
    }
    void addVideo(String video){
        if(size<10){
            list[size++]=new Video(video);
        }
        else{
            System.out.println("Playlist is full.");
        }
    }
    void returnVideo(String video){
        for(int i=0;i<size;i++){
            if(list[i].getTitle().equals(video) && list[i].isCheckedOut()){
                list[i].returnVideo();
                System.out.println("Video with title "+video+" is returned.");
                return;
            }
        }
        System.out.println("Video not found.");
    }
    void checkOut(String video){
        for(int i=0;i<size;i++){
            if(list[i].getTitle().equals(video) && !list[i].isCheckedOut()){
                list[i].checkOut();
                System.out.println("Video with title "+video+" is rented out.");
                return;
            }
        }
        System.out.println("Video not found.");
    }
    void receiveRating(String video, float rat){
        for(int i=0;i<size;i++){
            if(list[i].getTitle().equals(video)){
                list[i].giveRating(rat);
                return;
            }
        }
        System.out.println("Video not found.");
    }
    void listInventory(){
        for(int i=0;i<size;i++){
            if(!list[i].isCheckedOut()){
                System.out.print("Title of the video is: "+list[i].getTitle());
                System.out.print(" and its rating is: "+list[i].getRating());
                System.out.println();
            }
        }
    }
}
public class Exp2 {
    public static void main(String[] args){
        Scanner in=new Scanner(System.in);
        VideoStore playlist=new VideoStore();
        for(int i=0;i<3;i++){
            System.out.println("Enter video title:");
            String video=in.nextLine();
            System.out.println("Enter rating:");
            float rating=in.nextInt();
            playlist.addVideo(video);
            playlist.receiveRating(video, rating);
            in.nextLine();
        }
        System.out.println("Enter video title for renting:");
        String title=in.nextLine();
        playlist.checkOut(title);
        System.out.println("Videos in the playlist are:");
        playlist.listInventory();
        in.close();
    }
}
