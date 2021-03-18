
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.SQLManager;

public class QueueSimulator {

	public static void main(String[] args) {
		SimWindow frame = new SimWindow("待ち行列シミュレーター", 1000, 800);
		frame.setVisible(true);
	}

}

class SimWindow extends JFrame implements ActionListener{

	private JLabel label_arrival_interval;
	private JTextField field_arrival_interval;
	private JLabel label_service_interval;
	private JTextField field_service_interval;
	private JLabel label_line_num;
	private JTextField field_line_num;
	private JLabel label_staff_num;
	private JTextField field_staff_num;

	private JButton button_execute;
	private JLabel label_test;

	private JLabel label_ideal_value;
	public static JLabel label_actual_value;

	private DrawCanvas canv_imageCanvas;
	private CalculateQueue cal_queue;

	SimWindow(String title, int width, int height){
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setResizable(false);


		label_arrival_interval = new JLabel("平均到着間隔(分)：");
		field_arrival_interval = new JTextField("7", 5);
		label_service_interval = new JLabel("平均サービス間隔(分)：");
		field_service_interval = new JTextField("4", 5);
		label_line_num = new JLabel("行列数：");
		field_line_num = new JTextField("1", 5);
		label_staff_num = new JLabel("窓口数：");
		field_staff_num = new JTextField("2", 5);

		button_execute = new JButton("Start");
		button_execute.addActionListener(this);
		label_test = new JLabel();

		// 平均待ち時間
		label_ideal_value = new JLabel("待ち時間 理論値：");
		label_actual_value = new JLabel("待ち時間 実測値：");

		DrawCanvas canvas = new DrawCanvas(width, height);
		this.add(canvas);
		Panel panel_inputs = new Panel();
		panel_inputs.setPreferredSize(new Dimension(width*2/5, height/3));
		Panel panel_results = new Panel();
		panel_results.setPreferredSize(new Dimension(width*2/5, height/3));
		Panel panel_images = new Panel();
		panel_images.setPreferredSize(new Dimension(width, height*2/3));
		canvas.setLayout(new BorderLayout());
		canvas.add("West", panel_inputs);
		canvas.add("East", panel_results);
		canvas.add("South", panel_images);

		panel_inputs.setLayout(new GridLayout(5,2));
		panel_inputs.add(label_arrival_interval);
		panel_inputs.add(field_arrival_interval);
		panel_inputs.add(label_service_interval);
		panel_inputs.add(field_service_interval);
		//panel_inputs.add(label_line_num);
		//panel_inputs.add(field_line_num);
		panel_inputs.add(label_staff_num);
		panel_inputs.add(field_staff_num);
		panel_inputs.add(button_execute);
		panel_inputs.add(label_test);

		panel_results.setLayout(new GridLayout(2,1));
		panel_results.add(label_ideal_value);
		panel_results.add(label_actual_value);

		panel_images.setLayout(new GridLayout(1,1));
		canv_imageCanvas = new DrawCanvas(width, height/2);
		panel_images.add(canv_imageCanvas);

		// タイマー
		Timer timer = new Timer();
		timer.schedule(canv_imageCanvas.getTask(), 1l, 10l);

		cal_queue = new CalculateQueue();
	}

	public void actionPerformed(ActionEvent e) {
		CalculateQueue.setCustomers(new ArrayList<String>());
		int arrival_interval = Integer.parseInt(field_arrival_interval.getText());
		int service_interval = Integer.parseInt(field_service_interval.getText());
		int line_num = Integer.parseInt(field_line_num.getText());
		canv_imageCanvas.setLineNum(line_num);
		int staff_num = Integer.parseInt(field_staff_num.getText());
		canv_imageCanvas.setStaffNum(staff_num);
		cal_queue.setStaffNum(staff_num);
		String[] processing_customer = new String[staff_num];
		for(int i=0;i<processing_customer.length;i++) processing_customer[i]=null;
		CalculateQueue.setProcessingCustomers(processing_customer);
		double arrival_rate = 1.0/arrival_interval;
		cal_queue.setArrivalRate(arrival_rate);
		double service_rate = 1.0/service_interval;
		cal_queue.setServiceRate(service_rate);
		double rho = arrival_rate/service_rate;
		label_ideal_value.setText("待ち時間 理論値："+service_interval*Math.pow(rho,staff_num)/(1-Math.pow(rho,staff_num))+"分");
		//label_actual_value.setText("実測値："+service_interval*CalculateQueue.getCustomers().size()+"s");

		Timer timer2 = new Timer();
		timer2.schedule(cal_queue.getTask(), 1l, 100l);
	}
}

class DrawCanvas extends JPanel{

	private int canvas_width;
	private int canvas_height;
	private int line_num;
	private int staff_num;
	private Image background;
	private Image img_customer = Toolkit.getDefaultToolkit().getImage("./src/image/customer.png");
	private Image img_staff = Toolkit.getDefaultToolkit().getImage("./src/image/staff.png");
	private double expanse_rate = 0.9;

	private TimerTask task;

	public DrawCanvas(int width, int height) {
		super();
		this.canvas_width = width;
		this.canvas_height = height;
		this.background = createImage(width, height);
		this.task = new TimerTask() {
			public void run() {
				repaint();
			}
		};
	}

	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		g.drawImage(this.background, 0, 0, this);
		//列の表示
		for(int i=0;i<CalculateQueue.getCustomers().size();i++) {
			g.drawImage(this.img_customer, this.canvas_width/2-this.img_customer.getWidth(null)*i, this.canvas_height/2,
					(int)(this.img_customer.getWidth(null)*this.expanse_rate),
					(int)(this.img_customer.getHeight(null)*this.expanse_rate),
					this);
			g.drawString(CalculateQueue.getCustomers().get(i),
					this.canvas_width/2-this.img_customer.getWidth(null)*i, this.canvas_height/2+this.img_customer.getHeight(null));
		}
		//窓口描画
		for(int i=0;i<this.staff_num;i++) {
			g.drawImage(this.img_staff, this.canvas_width*17/20, this.canvas_height*i/staff_num,
					(int)(this.img_staff.getWidth(null)*this.expanse_rate),
					(int)(this.img_staff.getHeight(null)*this.expanse_rate),
					this);
			g.drawString(CalculateQueue.getStaffs()[i],
					this.canvas_width*17/20, this.canvas_height*i/staff_num+this.img_staff.getHeight(null));
		}
		//受付中の客描画
		for(int i=0;i<this.staff_num;i++) {
			if(CalculateQueue.getProcessingCustomers()[i]!=null) {
				g.drawImage(this.img_customer, this.canvas_width*7/10, this.canvas_height*i/staff_num,
						(int)(this.img_customer.getWidth(null)*this.expanse_rate),
						(int)(this.img_customer.getHeight(null)*this.expanse_rate),
						this);
				g.drawString(CalculateQueue.getProcessingCustomers()[i],
						this.canvas_width*7/10, this.canvas_height*i/staff_num+this.img_customer.getHeight(null));
			}
		}

	}

	public TimerTask getTask() {
		return this.task;
	}

	public void setLineNum(int line_num) {
		this.line_num = line_num;
	}

	public void setStaffNum(int staff_num) {
		this.staff_num = staff_num;
	}
}

class CalculateQueue {
	private Random random_jen = new Random();
	private int random_int = 0;
	private TimerTask task;
	private static List<String> customers = new ArrayList<String>();
	private static String[] processing_customers;
	private static String[] staffs;
	private double arrival_rate;
	private double service_rate;
	private int staff_num;
	private SQLManager sql_manager = new SQLManager(new String[]{"root", "basekinohara", "sakila"});
	private List<String> customer_name_list = sql_manager.getCustomerNameList();
	private List<String> staff_name_list = sql_manager.getStaffNameList();
	private List<Double> actual_values = new ArrayList<>();

	public CalculateQueue() {
		this.task = new TimerTask() {
			public void run() {
				//客の追加
				random_int = random_jen.nextInt(100);
				if(random_int < 100*arrival_rate) {
					System.out.println("add");
					for(int i=0;i<staff_num;i++) {
						String name = customer_name_list.get(random_jen.nextInt(customer_name_list.size()));
						if(processing_customers[i]==null) {
							processing_customers[i] = name;
							break;
						} else if (i==staff_num-1){
							customers.add(name);
						}
					}
				}
				//客を捌く
				for(int i=0;i<staff_num;i++) {
					random_int = random_jen.nextInt(100);
					if(random_int < 100*service_rate && processing_customers[i]!=null) {
						System.out.println("remove");
						if(customers.size()!=0) {
							processing_customers[i] = customers.get(0);
							customers.remove(0);
						} else {
							processing_customers[i] = null;
						}
					}
				}
				int empty_register = 1;
				for(String name: processing_customers) if(name == null) empty_register = 0;
				actual_values.add((CalculateQueue.getCustomers().size()+empty_register)/service_rate);
				SimWindow.label_actual_value.setText("待ち時間 実測値："+calMean(actual_values)+"分");
			}
		};
	}

	public TimerTask getTask() {
		return this.task;
	}

	public int getRandom() {
		return this.random_int;
	}

	public void setArrivalRate(double arrival_rate) {
		this.arrival_rate = arrival_rate;
	}

	public void setServiceRate(double service_rate) {
		this.service_rate = service_rate;
	}

	public static List<String> getCustomers(){
		return customers;
	}

	public static void setCustomers(List<String> s) {
		customers = s;
	}

	public void setStaffNum(int staff_num) {
		this.staff_num = staff_num;
		staffs = new String[staff_num];
		for(int i=0;i<staff_num;i++) {
			staffs[i] = staff_name_list.get(i);
		}
	}

	public static String[] getStaffs(){
		return staffs;
	}

	public static String[] getProcessingCustomers(){
		return processing_customers;
	}

	public static void setProcessingCustomers(String[] s) {
		processing_customers = s;
	}

	public double calMean(List<Double> actual) {
		double result = 0;
		for(double i: actual) {
			result += i/actual.size();
		}
		return result;
	}

}