package tomasulo;

public class TomasuloTestCase {

	public TomasuloTestCase() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		public Instruction(int rs, int rt, int rd, int imm, String type) {
		//F6 = 6
		//R2 = 2
		//R3 = 3
		//F2 = 1
		//F0 = 0
		//F4  = 4
		//F8 = 7
		
		Instruction I0 = new Instruction(2,6,-1, 32, "load");
		Instruction I1 = new Instruction(3,1,-1, 44, "load");
		Instruction I2 = new Instruction(1,4,0, 0, "multiply");
		Instruction I3 = new Instruction(1,6,7, 0, "subtract");
		String[] Fu_info = {"add,3,2","multiply,2,6","load,2,2"};
		Tomasulo tomasulo = new Tomasulo(6, 4, Fu_info, 0, 100);
		tomasulo.instruction_buffer.addToBuffer(I0);
		tomasulo.instruction_buffer.addToBuffer(I1);
		tomasulo.instruction_buffer.addToBuffer(I2);
		tomasulo.instruction_buffer.addToBuffer(I3);
//		for(int i = 0; i < tomasulo.reservation_stations.length; i++) {
//			System.out.println(tomasulo.reservation_stations[i].name);
//			System.out.println(tomasulo.reservation_stations[i].name.contains("add"));
//			System.out.println(tomasulo.reservation_stations[i].busy);

//		}
		tomasulo.Simulation();


	}

}
