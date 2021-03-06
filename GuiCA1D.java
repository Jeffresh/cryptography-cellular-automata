import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
     * GuiTemplate.java
     * Purpose: this program
     * implements a Gui template that you can modify and adapt easily for any application
     * that need data visualization.
     * @author: Jeffrey Pallarés Núñez.
     * @version: 1.0 23/07/19
     */

public class GuiCA1D extends Frame implements ActionListener, FocusListener {

    private static final long serialVersionUID = 1L;

    private static JMenuBar nav_bar;
    private static String[] buttons_names;
    private static Map<String, JButton> gui_buttons = new LinkedHashMap<String, JButton>();
    public static Map<String, String> textfields_and_labels = new LinkedHashMap<>();
    private static JComboBox<String>  generator_list_combo_box;
    private static String[] engine_generator_names  = {
            "Basic","generator261a", "generator261b", "generator262", "generator263", "generatorFishmanAndMore1",
            "generatorFishmanAndMore2", "generatorRandu","generatorCombinedWXY",
    };
    private static String initializer_mode = "Basic";


    private JMenuBar createTopBar(Color color, Dimension dimension) {

        JMenuBar top_bar = new JMenuBar();
        top_bar.setOpaque(true);
        top_bar.setBackground(color);
        top_bar.setPreferredSize(dimension);

        return top_bar;
    }


    private JMenu createMenu( String menu_name, Font font, Color color) {

        JMenu menu = new JMenu(menu_name);
        menu.setFont(font);
        menu.setForeground(color);
        return menu;
    }


    private  Map<String, JMenu> createMenusItems( Map<String,String[]> items, Color color, Font font) {

        JMenuItem item;
        JMenu m;
        Map<String, JMenu> menus = new HashMap<>();

        for(Map.Entry<String,String[]> menu: items.entrySet()){
            String menu_name = menu.getKey();
            m = createMenu(menu_name, font , color);
            for(String item_name :menu.getValue()) {
                item = new JMenuItem(item_name);
                item.setFont(font);
                item.addActionListener(this);
                m.add(item);
            }
            menus.put(menu_name, m);
        }

        return menus;
    }

    private JMenuBar createNavBar() {

        Font menu_font = new Font("Dialog", Font.PLAIN, 20);
        Color menu_font_color = new Color(168, 168, 168);
        Color navbar_color = new Color(0,0,0);
        Dimension navbar_dimension = new Dimension(200,40);
        Map<String, String[] > menu_items = new HashMap<>();

        menu_items.put("File", new String[]{"Open File", "Save File"});
        menu_items.put("Help", new String[]{"Help message"});
        menu_items.put("About", new String[]{"About message"});

        nav_bar = createTopBar(navbar_color, navbar_dimension);

        Map<String, JMenu> menus = createMenusItems(menu_items, menu_font_color, menu_font);

        nav_bar.add(menus.get("File"));
        nav_bar.add(Box.createHorizontalGlue());
        nav_bar.add(menus.get("Help"));
        nav_bar.add(menus.get("About"));
        fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        log = new JTextArea(5,20);

        return nav_bar;
    }

//
    Map<String, JRadioButton> cilindric_frontier_buttons = new HashMap<>();

    private Map<String, JRadioButton> createRadioButton(String[] round_buttons_options, ButtonGroup group){
        Map<String, JRadioButton> radio_button = new HashMap<>();
        for(String button_name: round_buttons_options){
            JRadioButton button = new JRadioButton(button_name);
            button.setFont(new Font(null, Font.PLAIN,20));
            button.setMnemonic(KeyEvent.VK_B);
            button.setActionCommand(button_name);
            button.setSelected(true);
            button.addActionListener(this);
            group.add(button);
            radio_button.put(button_name, button);

        }
        return radio_button;
    }

    private Map<String, JButton> createButtons(String[] button_names){

        Map<String, JButton> buttons_dict = new HashMap<String, JButton>();
        JButton button;

        for (String name: button_names) {
            button = new JButton(name);
            button.addActionListener(this);
            buttons_dict.put(name, button);
        }

        return buttons_dict;
    }

    private JPanel createButtonsPane(){

        gui_buttons = createButtons(buttons_names);
        JPanel buttons_pane = new JPanel();
        for(String button_name: buttons_names)
            buttons_pane.add(gui_buttons.get(button_name), BorderLayout.CENTER);

        buttons_pane.setPreferredSize(new Dimension(100, 5));
        buttons_pane.setMaximumSize(new Dimension(100, 5));
        buttons_pane.setMinimumSize(new Dimension(100, 5));
        buttons_pane.setOpaque(true);

        buttons_pane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Control"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));

        return buttons_pane;
    }

    private Object[] createTextFieldsAndLabels(Map<String, String> texts_labels){
        JLabel[] labels = new JLabel[texts_labels.size()];
        JTextField[] textFields = new JTextField[texts_labels.size()];
        int index = 0;

        for(Map.Entry<String, String> text_label: texts_labels.entrySet()){
            textFields[index] = new JTextField();
            textFields[index].setText(text_label.getValue());
            textFields[index].addFocusListener(this);
            labels[index] = new JLabel(text_label.getKey());
            labels[index].setLabelFor(textFields[index]);
            index++;
        }
        return new Object[]{labels, textFields};
    }

    private static JTextField[] input_variables_textfields;
    private static JLabel [] input_variables_labels;
    private static JLabel [] combobox_labels= {new JLabel("Initializer mode")};
    private static JLabel [] radio_button_labels = { new JLabel("Cilindric Frontier")};

    private static void initializeInputTextFieldsAndLabels(){
        textfields_and_labels.put("CA-Rule: ", "90");
        textfields_and_labels.put("Key: ", "pass");
    }
    private static void initializeButtonNames(){
        buttons_names = new String[]{"Initialize", "Start", "Stop"};
    }

    private JSplitPane createGuiPanels() {


        Object[]  labels_and_textfields_list = createTextFieldsAndLabels(textfields_and_labels);

        generator_list_combo_box = new JComboBox<>(engine_generator_names);
        generator_list_combo_box.addFocusListener(this);

        JComboBox[] combo_box_list = {generator_list_combo_box};

        JPanel input_variables_pane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();

        input_variables_pane.setLayout(gridbag);
        input_variables_pane.setPreferredSize(new Dimension(100, 900));
        input_variables_pane.setMinimumSize(new Dimension(100, 900));

        input_variables_labels = (JLabel[]) labels_and_textfields_list[0];
        input_variables_textfields = (JTextField[]) labels_and_textfields_list[1];

//        cilindric_frontier_buttons = createRadioButton(round_buttons_options,cilindric_frontier);

        addLabelTextRows(input_variables_labels,input_variables_textfields, combobox_labels, combo_box_list,
                radio_button_labels, cilindric_frontier_buttons,
                input_variables_pane);

        input_variables_pane.setBorder(
                                   BorderFactory.createCompoundBorder(
                                                                      BorderFactory.createTitledBorder("Variables"),
                                                                      BorderFactory.createEmptyBorder(5,5,5,5)));
        input_variables_pane.setOpaque(true);
        JPanel buttons_pane = createButtonsPane();

        JSplitPane control_center_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              input_variables_pane,
                                              buttons_pane);
        control_center_pane.setMaximumSize(new Dimension(800,800));
        control_center_pane.setMinimumSize(new Dimension(800,800));
        input_variables_pane.setMaximumSize(new Dimension(800,800));
        input_variables_pane.setMinimumSize(new Dimension(800,800));

        control_center_pane.setOneTouchExpandable(true);
        control_center_pane.setOpaque(true);

        return control_center_pane;
    }

    private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, JLabel[] combobox_labels,
                                  JComboBox<String>[] combo_box_list,JLabel[] radio_labels,
                                  Map<String, JRadioButton> radiobutton,
                                  Container container){

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        int numLabels = labels.length;
        int num_labels_combobox = combobox_labels.length;

        for (int i = 0; i < numLabels; i++){

        	labels[i].setFont(new Font(null, Font.PLAIN,20));
        	textFields[i].setFont(new Font(null, Font.PLAIN,20));
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 1.0;                       //reset to default
            container.add(labels[i], c);
 
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.NONE;
            c.weightx = 1.0;
            textFields[i].setColumns(3);
            container.add(textFields[i], c);
        }



    }

    private static  String input_loaded_text;
    private static JFileChooser fc;
    private static JTextArea log;
    private static JPanel canvas;
    private static JTextArea input_area;
    private static JTextArea output_area;
    private static  JSplitPane encryption_area;


    private static JPanel createEncryptionArea(String input_area_text){
        JPanel canvas = new JPanel();
        input_area = new JTextArea(input_area_text);
        output_area = new JTextArea();

        input_area.setPreferredSize(new Dimension(700, 920));
        input_area.setMaximumSize(new Dimension(700, 920));

        output_area.setPreferredSize(new Dimension(700, 920));

        encryption_area = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, input_area, output_area);

        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Encrypt"),
                BorderFactory.createEmptyBorder(5,5,5,5)));

        canvas.setPreferredSize(new Dimension(1600, 1600));
        canvas.setMinimumSize(new Dimension(1600, 1600));
        canvas.add(encryption_area);

        return canvas;
    }


    private static void createAndShowGUI(){

        chooseInputVariables(1,1,2);
        initializeButtonNames();
        initializeInputTextFieldsAndLabels();

        JFrame frame = new JFrame("Cryptography cellular automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(500,500));
        frame.setJMenuBar(new GuiCA1D().createNavBar());

        int xMax = cells_number;
        int yMax = generations;
//        canvas_template = new MainCanvas(xMax, yMax);
//        canvas_template.setOpaque(true);
//        canvas_template.setDoubleBuffered(false);
//        canvas_template.setPreferredSize(new Dimension(1000, 1000));


        canvas =createEncryptionArea("Write here a message to be encrypted");

        JSplitPane buttons = new GuiCA1D().createGuiPanels();
        JSplitPane window = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,canvas, buttons);
        window.setOneTouchExpandable(true);
        window.setOpaque(true);
        window.setOneTouchExpandable(true);
        frame.pack();
        frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.setContentPane(window);

    }

    private static SwingWorker<Void, GuiCA1D> worker;

    private static MainCanvas canvas_template;

    private static double numeric_var = 33 ;
    private static String string_var = "Hello World";
    private static JLabel label_numeric_var_value;

    private static void chooseInputVariables(int n_string_variables, int n_numeric_variables, int n_label_variables){
        input_numeric_variables = new Double[n_numeric_variables];
        input_string_variables = new String[n_string_variables];
        input_label_variables = new JLabel[n_label_variables];
    }

    private static String[] input_string_variables;
    private static Double[] input_numeric_variables;
    private static JLabel[] input_label_variables;

    private static JLabel label_string_var_value;
    private static int value = 0;

    public void showURI(String uri){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void deleteCanvasLabels( JLabel[] labels){

        if(label_numeric_var_value != null) canvas_template.remove(label_numeric_var_value);
        if(label_string_var_value != null) canvas_template.remove(label_string_var_value);
    }

    private static int seed = 1;
    private static int states_number = 2;
    private static int neighborhood_range = 1;
    private static int transition_function = 90;
    private static int cfrontier = 0;
    private static int cells_number = 1000;
    private static int generations = 1000;
    private static int cell_spatial_entropy = 499;
    private static String password = "pass";

    private String stringToBinary(String password_plain){
        byte[] bytes = password_plain.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');

        }

        return binary.toString();
    }

    private String binaryToString(String binary_values){

        String str = Arrays.stream(String.valueOf(binary_values).split("(?<=\\G.{8})"))
                .parallel()
                .map(eightBits -> (char)Integer.parseInt(eightBits, 2))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                ).toString();

        return str;
    }

    private String encryptMessage(String message){


        byte[]bytes = new byte[message.length()];
        try{
            bytes = message.getBytes("ISO-8859-1");}catch(Exception e){}
        StringBuilder binary_message = new StringBuilder();

        for(byte b : bytes)
        {
            int val =b;
            for(int i =0 ; i<8 ; i++)
            {
                binary_message.append((val&128)==0?'0':'1');
                val<<=1;
            }

            binary_message.append(" ");
        }

        int n_bits = binary_message.length();

        CellularAutomata1D ca1d = new CellularAutomata1D();
        ca1d.initializer(n_bits, generations, states_number,
                neighborhood_range, transition_function, seed, cfrontier , initializer_mode, n_bits/2);

        String binary_pass = stringToBinary(password);
        ca1d.initializeStatePassword(binary_pass);

        char[] cad = new char[binary_message.length()];

        for(int i = 0; i<cad.length; i ++)
        {

            if(binary_message.charAt(i)!=' ')
            {
                if(ca1d.getActualState()[getWidth()/2] == 0 && binary_message.charAt(i) == '0' ||
                        ca1d.getActualState()[getWidth()/2] == 1 && binary_message.charAt(i) == '1')
                    cad[i] = '0';
                else
                    cad[i]='1';
            }
            else
                cad[i]= ' ';


            try{ca1d.nextGen(i);}catch(Exception e){};
            CellularAutomata1D.changeRefs();

        }

        String aux = new String(cad);

        String[] palabras = aux.split(" ");
        StringBuilder cadf = new StringBuilder();



        for(int i = 0; i<palabras.length; i++)
            cadf.append((char)Integer.parseInt(palabras[i],2));


        return cadf.toString();

    }


    public void actionPerformed( ActionEvent e) {

        if(e.getSource() == nav_bar.getMenu(0).getItem(0)) {
            input_loaded_text = new String();
            Scanner cout = null;
            int returnVal = fc.showOpenDialog(fc);

            if(returnVal ==JFileChooser.APPROVE_OPTION)
            {

                File file = fc.getSelectedFile();
                log.append("Opening: "+file.getName()+"."+'\n');

                try
                {
                    cout = new Scanner(file);
                    while (cout.hasNextLine()){
                        input_loaded_text += cout.nextLine();     // Guardamos la linea en un String
//                        System.out.println(input_loaded_text);
                    }
                }catch(Exception ex){}
            }

            else
            {
                log.append("Open command cancelled by user."+'\n');

            }

            log.setCaretPosition(log.getDocument().getLength());

            input_area.setText(input_loaded_text);
            output_area.setText("");

        }

        if(e.getSource() == nav_bar.getMenu(0).getItem(1)) {
//            value = 3;
//            deleteCanvasLabels(input_variables_labels);
//            MainCanvas.task.initializer(cells_number, generations, states_number,
//                    neighborhood_range, transition_function, seed, cfrontier , initializer_mode, cell_spatial_entropy);
//            canvas_template.updateCanvas();
        }


        if(e.getSource()==nav_bar.getMenu(2).getItem(0)) {
            String uri = "https://docs.oracle.com/javase/7/docs/api/javax/swing/package-summary.html";
            showURI(uri);
        }

        if(e.getSource()==nav_bar.getMenu(3).getItem(0)) {
            String uri = "https://github.com/Jeffresh";
            showURI(uri);
        }

        if(e.getSource() == gui_buttons.get(buttons_names[0])) {
            String message =new String();

            message = input_area.getText();

            System.out.println(message);

            String encrypted_message = encryptMessage(message);

            System.out.print(encrypted_message);

            output_area.setText(encrypted_message);


            System.out.println("Cells number: "+cells_number);
            System.out.println("Generations: "+generations);
            System.out.println("State number: "+states_number);
            System.out.println("Neighborhood Range: "+ neighborhood_range);
            System.out.println("Transition_function: "+ transition_function);
            System.out.println("Seed: "+seed);
            System.out.println("Initializer mode: "+initializer_mode);
            System.out.println("Cell Spatial Entropy: "+cell_spatial_entropy);
            System.out.println("Password: "+password);

        }

        if(e.getSource()== gui_buttons.get(buttons_names[1])) {
            worker = new SwingWorker<Void, GuiCA1D>()
            {
                @Override
                protected Void doInBackground() {
                    try{
                        MainCanvas.task.caComputation(generations);
                        String message = "\"Temporal entropy\"\n"
                                + "cell: "+cell_spatial_entropy+"\n"
                                + "Spatial entropy: "+MainCanvas.task.getTemporalEntropy();
                        JFrame dialog =  new JFrame();
                        dialog.setAlwaysOnTop(true);
                        JOptionPane.showMessageDialog(dialog, message, "Dialog",
                                JOptionPane.INFORMATION_MESSAGE);

                    }
                    catch(Exception ex){System.out.println("Worker exception");}
                    return null;
                }
            };
            worker.execute();
        }

        if(e.getSource()== gui_buttons.get(buttons_names[2])) {
            worker.cancel(true);
            worker.cancel(false);
            CellularAutomata1D.stop();
        }

    }


    public void focusGained(FocusEvent e) {
    	//nothing
	}
	public void focusLost(FocusEvent e) {
        String nump;

        try {
                double nump_value;
                if (e.getSource() == input_variables_textfields[0]) {
                    nump = input_variables_textfields[0].getText();
                    nump_value = Double.parseDouble(nump);
                    if (nump.equals("") || (nump_value < 0 || nump_value >=255)) {
                        numeric_var = 0;
                        throw new Exception("Invalid Number");
                    }
                    transition_function = Integer.parseInt(nump);
                }
        }
            catch (Exception ex){
                String message = "\"Invalid Number\"\n"
                        + "Enter a number between 0 and 255\n"
                        + " setted 0 by default";
                JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
                        JOptionPane.ERROR_MESSAGE);
            }

        double string;
        if (e.getSource() == input_variables_textfields[1]) {
             password = input_variables_textfields[1].getText();
        }
    }
    
    public static void main(String[] args)
    {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.
                SwingUtilities.
                invokeLater(GuiCA1D::createAndShowGUI);
    }
}

