import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // make sure the file was opened properly
        File input = new File(args[0]);
        Scanner modules = null;
        try {
            modules = new Scanner(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // reading the number of modules in the file
        int num_modules;
        int num_definitions;
        int num_uses;

        int[] base_address = {0};
        int[] module_size = {0};

        //ArrayList<ArrayList<instruction>> instructions_per_module = new ArrayList<ArrayList<instruction>>();
        instruction[][] instructions_per_module = {{new instruction('x', 0)}};

        ArrayList<symbol> SymbolTable = new ArrayList<>();

        int i;
        int j;
        int k;
        int l;
        int m;

        for (k = 0; k < 2; k++) { //making sure I loop over everything twice
            if(k==1){
                try {
                    modules = new Scanner(input);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            num_modules = modules.nextInt();
            if (k == 0) {
                module_size = new int[num_modules];
                base_address = new int[num_modules];
                instructions_per_module = new instruction[num_modules][1000];
                base_address[0] = 0;
            }
            for (i = 0; i < num_modules; i++) {
                if (k == 0) { // declarations section
                    num_definitions = modules.nextInt();
                    for (j = 0; j < num_definitions; j++) {
                        symbol cur = new symbol(modules.next(), modules.nextInt(), i);
                        boolean duplicate = false;
                        for(int n = 0; n<SymbolTable.size(); n++){
                            if(SymbolTable.get(n).name.equals(cur.name)) {
                                System.err.println(cur.name + " defined multiple times.");
                                duplicate = true;
                            }
                        }
                        if(!duplicate){
                            SymbolTable.add(cur);
                        }
                    }
                } else { // skip this section on pass 2
                    num_definitions = modules.nextInt();
                    for (j = 0; j < num_definitions; j++) {
                        modules.next();
                        modules.nextInt();
                    }
                }
                if (k == 0) { // skip this section on pass 1
                    num_uses = modules.nextInt();
                    int instruction_num;
                    for (j = 0; j < num_uses; j++) {
                        modules.next();
                        instruction_num = modules.nextInt();
                        while (instruction_num != -1) {
                            instruction_num = modules.nextInt();
                        }
                    }
                } else { //uses section
                    num_uses = modules.nextInt();
                    String variable;
                    int instruction_num;
                    for (j = 0; j < num_uses; j++) {
                        variable = modules.next();
                        try {
                            instruction_num = modules.nextInt();
                        } catch (Exception e) {
                            System.out.println(modules.next());
                            System.exit(1);
                            instruction_num = 0;
                        }
                        while (instruction_num != -1) {
                            if(instruction_num >= module_size[i]){
                                System.err.println("Use exceeds module size.");
                            }
                            else if (instructions_per_module[i][instruction_num].used) {
                                System.err.println("Multiple variables used in same instruction.");
                                for(int n = 0; n<SymbolTable.size(); n++) {
                                    if(SymbolTable.get(n).name.equals(variable)){
                                        SymbolTable.get(n).used = true;
                                    }
                                }
                            } else if (instruction_num > module_size[i]) {
                                System.err.println(variable + " references an instruction outside the bounds of the current module.");
                            } else {
                                boolean exists = false;
                                for(int n = 0; n<SymbolTable.size(); n++){
                                    instructions_per_module[i][instruction_num].used = true;
                                    if(SymbolTable.get(n).name.equals(variable)) {
                                        instructions_per_module[i][instruction_num].address += SymbolTable.get(n).address;
                                        SymbolTable.get(n).used = true;
                                        n = SymbolTable.size();
                                        exists = true;
                                    }
                                }
                                if (!exists) {
                                    System.err.println(variable + " is used but never defined.");
                                    instructions_per_module[i][instruction_num].address = 0;
                                }
                            }
                            instruction_num = modules.nextInt();
                        }
                    }
                }
                if (k == 0) { // instructions section
                    module_size[i] = modules.nextInt();
                    for(int n = 0; n<SymbolTable.size(); n++){
                        if(SymbolTable.get(n).module == i){
                            if(SymbolTable.get(n).address >= module_size[i]){
                                System.err.println(SymbolTable.get(n).name + " uses an address larger than the bounds of the current module.");
                                SymbolTable.get(n).address = 0;
                            }
                            SymbolTable.get(n).address += base_address[i];
                            if(SymbolTable.get(n).address > 200){
                                SymbolTable.get(n).address = 0;
                                System.err.println(SymbolTable.get(n).name + " uses an address larger than the bounds of the current module.");
                            }
                        }
                    }
                    if (i < num_modules-1) {
                        base_address[i+1] = base_address[i] + module_size[i];
                    }
                    for (l = 0; l < module_size[i]; l++) {
                        try {
//                            instructions_per_module[i]=new instruction[module_size[i]];
                            instructions_per_module[i][l] = new instruction(modules.next().charAt(0), modules.nextInt());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // now relocate relative addresses and error check. Only resolve external on the second pass.
                        if (instructions_per_module[i][l].AEIR == 'R') {
                            if(instructions_per_module[i][l].address > module_size[i]){
                                instructions_per_module[i][l].address = 0;
                                System.err.println("Instruction uses an address larger than the module size.");
                            }
                            else {
                                instructions_per_module[i][l].address += base_address[i];
                            }
                        }
                        if (instructions_per_module[i][l].AEIR == 'E' && instructions_per_module[i][l].address>module_size[i]) {
                            instructions_per_module[i][l].address = 0;
                            System.err.println("Instruction uses an address larger than the module size.");
                        }
                        if (instructions_per_module[i][l].address > 200 && (instructions_per_module[i][l].AEIR == 'R' || instructions_per_module[i][l].AEIR == 'E' || instructions_per_module[i][l].AEIR == 'A')) {
                            instructions_per_module[i][l].address = 0;
                            System.err.println("Instruction uses an address larger than the memory space of the machine.");
                        }
                    }
                } else {
                    modules.nextInt();
                    for (l = 0; l < module_size[i]; l++) {
                        modules.next();
                        modules.nextInt();
                    }
                    System.out.println("Memory Map for Module " + (i+1));
                    for (m = 0; m < module_size[i]; m++) {
                        if(instructions_per_module[i][m].address > 99){
                            System.out.println(instructions_per_module[i][m].AEIR + " " + instructions_per_module[i][m].opcode + "" + instructions_per_module[i][m].address);
                        }
                        else if (instructions_per_module[i][m].address > 9){
                            System.out.println(instructions_per_module[i][m].AEIR + " " + instructions_per_module[i][m].opcode + "0" + instructions_per_module[i][m].address);
                        }
                        else{
                            System.out.println(instructions_per_module[i][m].AEIR + " " + instructions_per_module[i][m].opcode + "00" + instructions_per_module[i][m].address);
                        }
                    }
                }
            }
            modules.close();
        }
        System.out.println("Symbol Table:");
        for(int n = 0; n < SymbolTable.size(); n++) {
            if (!SymbolTable.get(n).used) {
                System.err.println("Warning: " + SymbolTable.get(n).name + " is defined but never used.");
            }
            System.out.println(SymbolTable.get(n).name + ": " + SymbolTable.get(n).address);
        }
    }
}

class symbol{
    String name;
    int address;
    boolean used = false;
    int module;
    public symbol(String name, int address, int module){
        this.name = name;
        this.address = address;
        this.module = module;
    }
}

class instruction {
    int opcode;
    int address;
    char AEIR;
    boolean used = false;
    public instruction(char AEIR, int stuff){
        this.AEIR = AEIR;
        this.opcode = stuff/1000;
        this.address = stuff%1000;
    }
}