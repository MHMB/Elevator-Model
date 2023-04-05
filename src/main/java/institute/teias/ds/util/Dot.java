package institute.teias.ds.util;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.incremental.mealy.dag.State;
import net.automatalib.words.Word;

import java.io.*;
import java.util.*;

public class Dot {
    public static void saveMealyMachineAsDot(CompactMealy<String, String> mealy, File f) throws Exception {
        System.out.println(f.getAbsolutePath());
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        List<Integer> states = new ArrayList<>();
        states.addAll(mealy.getStates()); states.removeAll(mealy.getInitialStates());
        states.addAll(0, mealy.getInitialStates());
        for (Integer si : states) {
            for (String in : mealy.getInputAlphabet()) {
                CompactMealyTransition<String> tr = mealy.getTransition(si,in);
                String out = tr.getOutput();
                int sj = tr.getSuccId();
//                bw.append(String.format("%d -- %s / %s -> %d\n", si,in,out,sj));
                bw.append(String.format("s%d -> s%d [label=\"%s  /  %s\"];\n", si,sj,in,out));
            }
        }
        bw.close();
    }

//    public static void writeFile(String filePath, String line) throws IOException {
//        File myObj = new File(filePath);
//        FileWriter myWriter = new FileWriter(myObj, true);
//        myWriter.write(line);
//        myWriter.close();
//    }

}
