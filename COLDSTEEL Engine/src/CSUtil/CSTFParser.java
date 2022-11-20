package CSUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

import CSUtil.DataStructures.Tuple2;
import Core.Executor;


/**
 * CStf files are text, human readable files housing all needed data for the COLDSTEEL engine to interact with its content.
 * This class can be created to write and parse those files.
 * 
 * Instances of this class are passed a writer and do not manage any file writing object on their own. 
 * 
 * CStf files are made up of labels and values. 
 * 
 * Lists are written by listing their elements sequentially with a linefeed following every entry.
 * 
 * Arrays and singular values are written inline with their label. Array values are comma separated. 
 * 
 * @author Chris Brown
 *
 */
public class CSTFParser {
	
	private BufferedWriter writer;
	private BufferedReader reader;
	
	public CSTFParser(BufferedWriter writer) {
		
		this.writer = writer;
		
	}

	public CSTFParser(BufferedReader reader) {
		
		this.reader = reader;
		
	}
	
	private int scope = 0;	
	private boolean waitingForValue = false; //used to ensure the user does not try to write two labels before a value.
	
	private void writeScope() throws IOException {
		
		for(int i = 0 ; i < scope ; i ++) writer.write("\t");
		
	}
	
	private void verifyScope() {
		
		assert scope >= 0: "Error: Too many values have been written without accompanying labels.";
 		
	}
	
	public static final String arrayToString(int... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) res += arr[i] + ",";
		res += arr[arr.length - 1] ;
		return res;
		
	}

	public static final String arrayToString(float... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) { 
		
			if(arr[i] % 1 == 0) res += (int)arr[i] + ","; 
			else res += arr[i] + ",";
		}
		
		if(arr[arr.length - 1] % 1 == 0) res += (int)arr[arr.length - 1];
		else res += arr[arr.length - 1];
		return res;
		
	}

	public static final String arrayToString(double... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) {
			
			if(arr[i] % 1 == 0) res += (long)arr[i] + ",";
			else res += arr[i] + ",";
			
		}

		if(arr[arr.length - 1] % 1 == 0) res += (long)arr[arr.length - 1];
		else res += arr[arr.length - 1];
		return res;
		
	}

	public static final String arrayToString(long... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) res += arr[i] + ",";
		res += arr[arr.length - 1] ;
		return res;
		
	}

	public static final String arrayToString(short... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) res += arr[i] + ",";
		res += arr[arr.length - 1] ;
		return res;
		
	}

	public static final String arrayToString(byte... arr) {
		
		String res = "";
		for(int i = 0 ; i < arr.length - 1; i ++) res += arr[i] + ",";
		res += arr[arr.length - 1] ;
		return res;
		
	}
	
	public void wname(String name) throws IOException {
		
		writer.write(name);
		writer.newLine();
		
	}
	
	public void wlist(String listName , int listSize) throws IOException {
		
		writeScope();
		writer.write(listName.replace(' ', '_') + " " + listSize + ":");
		scope += 1;
		writer.newLine();
		
	}

	public void wlist(String listName , int listSize , Executor listWritingFunction) throws IOException {
		
		writeScope();
		writer.write(listName.replace(' ', '_') + " " + listSize + ":");
		scope += 1;
		writer.newLine();
		listWritingFunction.execute();
		endList();
		
	}

	public void wlist(String listName) throws IOException {
		
		writeScope();		
		writer.write(listName.replace(' ', '_') + ":");
		scope += 1;
		writer.newLine();
		
	}

	public void endList() {
		
		scope -= 1;
		verifyScope();
		
	}
	
	public void wlabel(String labelName) throws IOException {
		
		if(waitingForValue) throw new IllegalStateException("Inconsistent application of labels and values. Two labels were added before a value");
		writeScope();
		writer.write(labelName.replace(' ', '_') + " -> ");
		waitingForValue = true;
				
	}
	
	public void wvalue(String value) throws IOException {
		
		writeScope();
		writer.write(value);
		writer.newLine();
		waitingForValue = false;
		
	}
	
	public void wlabelNullableValue(String label , String nullableValue) throws IOException {
		
		if(waitingForValue) throw new IllegalStateException("Inconsistent application of labels and values. Two labels were added before a value");
		writeScope();
		writer.write(label.replace(' ', '_') + " -> ");
		if(nullableValue == null) writer.write("null");
		else writer.write(nullableValue);
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wnullLabel(String label) throws IllegalStateException, IOException {
				
		wlabelValue(label , "null");
		
	}
	
	public void wlabelValue(String label , String value) throws IOException , IllegalStateException{
		
		if(waitingForValue) throw new IllegalStateException("Inconsistent application of labels and values. Two labels were added before a value");
		writeScope();
		writer.write(label.replace(' ', '_') + " -> ");
		writer.write(value);
		writer.newLine();
		waitingForValue = false;
		
	}
	
	public void wvalue(CharSequence value) throws IOException {
		
		writeScope();
		writer.write(value.toString());
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , CharSequence value) throws IOException {
		
		if(waitingForValue) throw new IllegalStateException("Inconsistent application of labels and values. Two labels were added before a value");
		writeScope();
		writer.write(label + " -> " + value);
		writer.newLine();
		waitingForValue = false;
		
	}
	
	public void wvalue(int... array) throws IOException {
		
		writeScope();
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , int... array) throws IOException {
		
		wlabel(label);//error check is done here already
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(float... array) throws IOException {
		
		writeScope();
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , float... array) throws IOException {
		
		wlabel(label);
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(double... array) throws IOException {
		
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , double... array) throws IOException {
		
		wlabel(label);
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(long... array) throws IOException {
		
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , long... array) throws IOException {
		
		wlabel(label);
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(short... array) throws IOException {
		
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}
	
	public void wlabelValue(String label , short... array) throws IOException {
		
		wlabel(label);
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(byte... array) throws IOException {
		
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wlabelValue(String label , byte... array) throws IOException {
		
		wlabel(label);
		writer.write(arrayToString(array));
		writer.newLine();
		waitingForValue = false;
		
	}

	public void wvalue(boolean bool) throws IOException {
		
		writer.write(bool ? "yes" : "no");
		writer.newLine();
		waitingForValue = false;
		
	}
	
	public void wlabelValue(String label , boolean bool) throws IOException {
		
		wlabel(label);
		writer.write(bool ? "yes" : "no");
		writer.newLine();
		waitingForValue = false;
		
	}


	//
	//READING
	//
		
	private String errorString(String expected , String callLine) {
		
		return "Invalid Call Site: expected label is " + expected + ", call site is " + callLine + ". scope: " + scope;
		
	}
	
	/**
	 * Reads and returns a line entirely on its own.
	 * 
	 * @param reader — file reader
	 * @return — a line of text
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rname() throws IOException {
		
		return reader.readLine();
		
	}
	
	/**
	 * Reads and returns a line as a pure value with no preceding label.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String rvalue() throws IOException {
		
		return reader.readLine().substring(scope);		
		
	}
	
	/**
	 * Reads and returns the string value of a label, asserting this method is called at an intended position in the reader's operation.
	 * 
	 * @param reader — file reader
	 * @param labelName — name of the label this method is being called on
	 * @return value associated with {@code labelName}
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public String rlabel(String labelName) throws IOException , AssertionError{
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName.replace(' ' , '_')) : errorString(labelName , label);
		return label.substring(label.indexOf(" -> ") + 4);
		
	}

	/**
	 * Reads and returns the value of a label, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @return the value associated with a label
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel() throws IOException {
		
		String label = reader.readLine().substring(scope);
		return label.substring(label.indexOf(" -> ") + 4);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws NullPointerException if {@code labelName} or {@code array} are null.
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , boolean... array) throws NullPointerException , IOException , AssertionError{
		
		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		array = new boolean[split.length];
		for(int i = 0 ; i < split.length ; i ++) array[i] = toBool(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws NullPointerException if {@code labelName} or {@code array} are null.
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , int... array) throws NullPointerException , IOException , AssertionError{
		
		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		array = new int[split.length];
		for(int i = 0 ; i < split.length ; i ++) array[i] = Integer.parseInt(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , float... array) throws IOException , AssertionError {

		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Float.parseFloat(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , double... array) throws IOException , AssertionError {

		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Double.parseDouble(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , long... array) throws IOException {

		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Long.parseLong(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , short... array) throws IOException {

		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Short.parseShort(split[i]);
		
	}

	/**
	 * Reads and stores a comma-separated list of primitive values into {@code array}, asserting this method is called 
	 * at an intended position in the reader's operation.
	 * 
	 * @param reader — file readers
	 * @param labelName — name of the label this method is being called on
	 * @param array — an array to store values parsed in
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a label other than {@code labelName}
	 */
	public void rlabel(String labelName , byte... array) throws IOException {

		Objects.requireNonNull(array);
		Objects.requireNonNull(labelName);
		
		String label = reader.readLine().substring(scope);
		assert label.contains(labelName) : errorString(labelName , label);
		label = label.substring(label.indexOf(" -> ") + 4);
		String[] split = label.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Byte.parseByte(split[i]);
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(boolean... array) throws IOException {

		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		String[] split = label.substring(label.indexOf(" -> ") + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = toBool(split[i]);
		return label;
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(int... array) throws IOException {

		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		//splits the value of the label
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Integer.parseInt(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(float... array) throws IOException {

		Objects.requireNonNull(array);
				
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Float.parseFloat(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(double... array) throws IOException {
		
		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Double.parseDouble(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(long... array) throws IOException {

		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Long.parseLong(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(short... array) throws IOException {

		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Short.parseShort(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	/**
	 * Reads and stores the values of a comma-separated list of primitive values into {@code array}, performing no safety checks.
	 * 
	 * @param reader — file reader
	 * @param array — an array to store parsed values
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlabel(byte... array) throws IOException {

		Objects.requireNonNull(array);
		
		String label = reader.readLine().substring(scope);
		int labelBreak = label.indexOf(" -> ");
		String[] split = label.substring(labelBreak + 4).split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Byte.parseByte(split[i]);
		return label.substring(0, labelBreak).replace('_', ' ');
		
	}

	public boolean rbooleanLabel(String labelName) throws IOException , AssertionError{
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return toBool(label.substring(label.indexOf(" -> ") + 4));
				
	}
	
	public int rintLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Integer.parseInt(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public float rfloatLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Float.parseFloat(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public long rlongLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Long.parseLong(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public double rdoubleLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Double.parseDouble(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public short rshortLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Short.parseShort(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public byte rbyteLabel(String labelName) throws IOException {
		
		String label = reader.readLine().substring(scope).replace('_', ' ');
		assert label.contains(labelName) : errorString(labelName , label);
		return Byte.parseByte(label.substring(label.indexOf(" -> ") + 4));
		
	}

	public void rvalue(int... array) throws IOException {
		
		Objects.requireNonNull(array);
		
		String value = reader.readLine().substring(scope);
		String[] split = value.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Integer.parseInt(split[i]);
				
	}
	
	public void rvalue(float... array) throws IOException {
		
		Objects.requireNonNull(array);
		
		String value = reader.readLine().substring(scope);
		String[] split = value.split(",");
		for(int i = 0 ; i < split.length ; i ++) array[i] = Float.parseFloat(split[i]);
				
	}
	
	/**
	 * 
	 * Reads a list header, asserting the list's name contains {@code listName}, and returning the number of elements in the list
	 * 
	 * @param reader — file reader 
	 * @param listName — name of list
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 * @throws AssertionError if this method is called on a list other than {@code listName}
	 */
	public int rlist(String listName) throws IOException , AssertionError {
		
		String list = reader.readLine().substring(scope);
		listName = listName.replace(' ', '_');
		assert list.contains(listName) : errorString(listName , list);
		String[] split = list.split(" ");
		//removes the colon at the end
		int size = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
		scope ++;
		return size;
		
	}
	
	public void rlist(Tuple2<String , Integer> result) throws IOException{
		
		Objects.requireNonNull(result);
		
		String list = reader.readLine().substring(scope);
		String[] split = list.split(" ");
		//removes the colon at the end
		int size = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
		scope ++;
		result.setFirst(split[0].replace('_', ' '));
		result.setSecond(size);
		
	}
	
	/**
	 * Reads a list header, returning the name of the header as given when the file was written.
	 * 
	 * @param reader — file reader 
	 * @return String representation of the header of a list
	 * @throws IOException if {@code reader} throws an error in its {@code readLine()} operation
	 */
	public String rlist() throws IOException {
		
		scope++;
		String line = reader.readLine();
		return line.substring(scope - 1 , line.length() - 1).replace('_', ' ');
		
	}

	public boolean rtest(String labelToTest) throws IOException {
		
		reader.mark(labelToTest.length() + scope);		
		String line = reader.readLine();
		if(line != null) {
			
			line = line.substring(scope);
			reader.reset();
			return line.contains(labelToTest);		
			
		}
		
		return false;
		
	}
	
	/**
	 * Marks the current position in the BufferedReader and checks the current line for containing both {@code labelToTest} and {@code "null"}. 
	 * Returns true if <b> both are present in the current line</b>. 
	 * 	 
	 * @param labelToTest
	 * @return
	 * @throws IOException
	 */
	public boolean rtestNull(String labelToTest) throws IOException {
		
		//length of the line is the scope + length of the string +" -> null" 
		
		reader.mark(labelToTest.length() + scope + 9);
		String label = reader.readLine().substring(scope).replace('_', ' ');
		boolean result = label.contains(labelToTest) && label.contains("null");
		reader.reset();
		return result;
		
	}

	/**
	 * Returns true if the value of the next label reads "null"
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean rtestNull() throws IOException {
		
		//length of the line is the scope + length of the string +" -> null" 
		
		reader.mark(999);
		String label = reader.readLine().substring(scope);
		boolean result = label.contains("null");
		reader.reset();
		return result;
		
	}
	
	public static boolean toBool(String text) { 
		
		if(text.equals("yes")) return true;
		else if (text.equals("no")) return false;
		else assert false : "Erorr: boolean failed to parse from: " + text;
		return false;
	}
		
}

