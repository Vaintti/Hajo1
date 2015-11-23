
public class Summaaja implements Runnable {
	public void run(){
		System.out.println("kakkaa");
	}
	public static void main(String[] args){
		(new Thread(new Summaaja())).start();
	}
}
