// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

package org.example;

import java.util.Comparator;
import java.util.Vector;
import java.io.*;

public class SchedulingAlgorithm {

  public static class processesComparator implements Comparator<sProcess> {
    @Override
    public int compare(sProcess proc1, sProcess proc2) {
      int process1, process2;

      if (proc1.cpudone == 0) {
        process1 = remainingTime(proc1.cputime, proc1.ioblocking);
      } else {
        process1 = remainingTime(proc1.cputime, proc1.cpudone);
      }

      if (proc2.cpudone == 0) {
        process2 = remainingTime(proc2.cputime, proc2.ioblocking);
      } else {
        process2 = remainingTime(proc2.cputime, proc2.cpudone);
      }

      return Integer.compare(process1, process2);
    }

    private int remainingTime(int cputime, int ioblocking) {
      if (cputime == 0) {
        return Integer.MAX_VALUE;
      }
      return cputime / ioblocking;
    }
  }

  public static Results Run(int runtime, Vector processVector, Results result) {
    int i;
    int comptime = 0;
    int completed = 0;
    int currentProcess = 0;
    int previousProcess;
    int size = processVector.size();
    String resultsFile = "Summary-Processes";

    result.schedulingType = "Preemptive";
    result.schedulingName = "Shortest Remaining Time Next";

    processesComparator comparator = new processesComparator();

    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      sProcess process = (sProcess) processVector.elementAt(currentProcess);
      out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
      processVector.sort(comparator);

      while (comptime < runtime) {
        if (process.cpudone == process.cputime) {
          completed++;
          out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          if (completed == size) {
            result.compuTime = comptime;
            out.close();
            return result;
          }
          for (i = size - 1; i >= 0; i--) {
            process = (sProcess) processVector.elementAt(i);
            if (process.cpudone < process.cputime) {
              currentProcess = i;
            }
          }
          process = (sProcess) processVector.elementAt(currentProcess);
          out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
        }
        if (process.ioblocking == process.ionext) {
          out.println("Process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          process.numblocked++;
          process.ionext = 0;
          previousProcess = currentProcess;
          for (i = size - 1; i >= 0; i--) {
            process = (sProcess) processVector.elementAt(i);
            if (process.cpudone < process.cputime && previousProcess != i) {
              currentProcess = i;
            }
          }
          process = (sProcess) processVector.elementAt(currentProcess);
          out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
        }
        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        comptime++;
        processVector.sort(comparator);
      }
    } catch (IOException e) { /* Handle exceptions */ }
    result.compuTime = comptime;
    return result;
  }
}