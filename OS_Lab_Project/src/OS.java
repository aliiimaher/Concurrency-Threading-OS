// Ali Maher 
// OS Lab Final Project
// 9932113

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;


// Process Check: 
// monitors all currently running processes with information about their resource usage
class ProcessCheck implements Runnable {
    @Override
    public void run() {
        System.out.println("Running Processes: \n");
        for (Thread process : OS.runningProcesses) {
            System.out.println("Process ID: " + process.getId());
            System.out.println("Process Name: " + process.getName());
            System.out.println("Resource Usage - CPU: " + OS.getCPUPercentage(process) +
                    ", RAM: " + OS.getRAMUsage(process) +
                    ", Bandwidth: " + OS.getBWUsage(process));
            System.out.println("= = = = = = = = = =");
        }
        System.out.println("System resources: \nCPU: " + OS.CPU + "\nRAM: " + OS.RAM + "\nBW: " + OS.BANDWIDTH);
    }
}

// all commands
enum Command {
    SLEEP,
    SUSPEND,
    KILL
}

// Process Manager:
// manages the process which its ID is given to it and apply the given command to it
class ProcessManager implements Runnable {
    int processID;
    String command;

    public ProcessManager(int processID, String command) {
        this.processID = processID;
        this.command = command;
    }

    public void run() {
        if (command.equals(Command.KILL.toString())) {
            // kill the process
            Thread thisThread = null;
            for (Thread thread : OS.runningProcesses) {
                if (thread.getId() == processID) {
                    thisThread = thread;
                    break;
                }
            }
            OS.CPU += OS.getCPUPercentage(thisThread);
            OS.RAM += OS.getRAMUsage(thisThread);
            OS.BANDWIDTH += OS.getBWUsage(thisThread);
            OS.runningProcesses.remove(thisThread);
            thisThread.stop();
            System.out.println(thisThread.getName() + " with processID(" + thisThread.getId() +
                    ") KILLED!");
        } else if (command.equals(Command.SUSPEND.toString())) {
            // suspend the process
            Thread thisThread = null;
            for (Thread thread : OS.runningProcesses) {
                if (thread.getId() == processID) {
                    thisThread = thread;
                    break;
                }
            }
            OS.CPU += OS.getCPUPercentage(thisThread);
            OS.RAM += OS.getRAMUsage(thisThread);
            OS.BANDWIDTH += OS.getBWUsage(thisThread);
            OS.runningProcesses.remove(thisThread);
            OS.waitingProcesses.add(thisThread);
            System.out.println(thisThread.getName() + " with processID(" + thisThread.getId() +
                    ") SUSPENDED(added to waiting queue)!");
        } else if (command.equals(Command.SLEEP.toString())) {
            // sleep the process (I set to sleep for 10 sec.)
            Thread thisThread = null;
            for (Thread thread : OS.runningProcesses) {
                if (thread.getId() == processID) {
                    thisThread = thread;
                    break;
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assert thisThread != null;
            System.out.println(thisThread.getName() + " with processID(" + thisThread.getId() +
                    ") SUSPENDED(added to waiting queue)!");
        }
    }
}

// VPN
class VPN implements Runnable {

    @Override
    public void run() {
        System.out.println("VPN process started...");
        OS.BANDWIDTH -= 2;
        while (true) {
            try {
                Thread.sleep(1000000);                                  // Idle/sleep state
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Mine
class Mine implements Runnable {
    Thread thisThread;

    @Override
    public void run() {

        OS.CPU -= 80;
        OS.RAM -= 4;
        OS.BANDWIDTH -= 8;

        System.out.println("Mine starts working...");
        try {
            Thread.sleep(30000);            // 30 sec.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Success! Mine finished its work.");

        OS.runningProcesses.remove(thisThread);
        OS.CPU += 80;
        OS.RAM += 4;
        OS.BANDWIDTH += 8;
    }
}

// Counter
class Counter implements Runnable {
    Thread thisThread;

    @Override
    public void run() {
        OS.CPU -= 10;
        OS.RAM -= 3;

        System.out.println("Counter process started...");
        int counter = 0;
        while (counter != 10000) {
            counter++;
        }
        System.out.println("Counter process completed. Last number: 10000");

        OS.runningProcesses.remove(thisThread);
        OS.CPU += 10;
        OS.RAM += 3;
    }
}


public class OS {
    // all resource mentioned in the project
    public static int CPU = 100;                                   // 100 percent
    public static int RAM = 6;                                     // 6 GB
    public static int BANDWIDTH = 10;                              // 10 Mbps 
    public static List<Thread> runningProcesses = new ArrayList<Thread>();
    public static List<Thread> waitingProcesses = new ArrayList<Thread>();

    public static int getCPUPercentage(Thread process) {
        return switch (process.getName()) {
            case "VPN" -> 0;
            case "Mine" -> 80;
            case "Counter" -> 10;
            default -> 0;
        };
    }

    public static int getRAMUsage(Thread process) {
        return switch (process.getName()) {
            case "VPN" -> 0;
            case "Mine" -> 4;
            case "Counter" -> 3;
            default -> 0;
        };
    }

    public static int getBWUsage(Thread process) {
        return switch (process.getName()) {
            case "VPN" -> 2;
            case "Mine" -> 8;
            case "Counter" -> 0;
            default -> 0;
        };
    }

    public static boolean checkResource(Thread thread) {
        return getCPUPercentage(thread) <= CPU &&
                getRAMUsage(thread) <= RAM && getBWUsage(thread) <= BANDWIDTH;
    }

    public static void sayBlockedInWaitingQueue(Thread thread) {
        System.out.println(thread.getName() + " is blocked in waiting queue!");
    }

    public static boolean findVPNForMine() {
        for (Thread thread: runningProcesses) {
            if (thread.getName().equals("VPN")) return true;
        }
        return false;
    }

    static Runnable waitingCheckRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    while (waitingProcesses.isEmpty()) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (checkResource(waitingProcesses.get(0))) {
                    Thread awakeThread = waitingProcesses.get(0);
                    waitingProcesses.remove(awakeThread);
                    runningProcesses.add(awakeThread);
                    awakeThread.start();
                    System.out.println("The process " + awakeThread.getName() +
                            " at the head of the waiting queue woke up.");
                }
            }
        }
    };

    public static void main(String[] args) {
        Thread checkWaitingQueue = new Thread(waitingCheckRunnable);
        checkWaitingQueue.start();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // defining menu options
            System.out.println("What do you want to do?");
            System.out.println("1. Check Processes");
            System.out.println("2. Process Manager");
            System.out.println("3. Create a VPN");
            System.out.println("4. Create a MINE");
            System.out.println("5. Create a COUNTER");
            System.out.println("6. Show Waiting Queue");
            System.out.println("0. Exit\n");
            System.out.println("====================\n");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                // Process Check
                case 1 -> {
                    ProcessCheck processCheck = new ProcessCheck();
                    Thread processCheckThread = new Thread(processCheck);
                    processCheckThread.setName("ProcessCheck");
                    processCheckThread.start();
                }

                // Process Manager
                case 2 -> {
                    System.out.println("Enter processID: ");
                    int processID = scanner.nextInt();
                    System.out.println("Enter command: ");
                    scanner.nextLine();
                    String command = scanner.nextLine();
//                    Command command = Command.KILL; // kill
                    ProcessManager processManager = new ProcessManager(processID, command);
                    Thread processManagerThread = new Thread(processManager);
                    processManagerThread.setName("ProcessManager");
                    processManagerThread.start();
                }

                // VPN
                case 3 -> {
                    VPN vpn = new VPN();
                    Thread vpnThread = new Thread(vpn);
                    vpnThread.setName("VPN");
                    if (checkResource(vpnThread)) {
                        runningProcesses.add(vpnThread);
                        vpnThread.start();
                    } else {
                        waitingProcesses.add(vpnThread);
                        sayBlockedInWaitingQueue(vpnThread);
                    }
                }

                // MINE
                case 4 -> {
                    Mine mine = new Mine();
                    Thread mineThread = new Thread(mine);
                    mine.thisThread = mineThread;
                    mineThread.setName("Mine");
                    if (checkResource(mineThread) && findVPNForMine()) {
                        runningProcesses.add(mineThread);
                        mineThread.start();
                    } else if (!findVPNForMine()){
                        System.out.println("Network Error!");
                    } else {
                        waitingProcesses.add(mineThread);
                        sayBlockedInWaitingQueue(mineThread);
                    }
                }

                // COUNTER
                case 5 -> {
                    Counter counter = new Counter();
                    Thread counterThread = new Thread(counter);
                    counterThread.setName("Counter");
                    counter.thisThread = counterThread;
                    if (checkResource(counterThread)) {
                        runningProcesses.add(counterThread);
                        counterThread.start();
                    } else {
                        waitingProcesses.add(counterThread);
                        sayBlockedInWaitingQueue(counterThread);
                    }
                }

                // Waiting Queue
                case 6 -> {
                    if (!waitingProcesses.isEmpty()) {
                        System.out.println("Blocked Processes: ");
                        for (Thread waitingThread: waitingProcesses) {
                            System.out.println("Process ID: " + waitingThread.getId());
                            System.out.println("Process Name: " + waitingThread.getName());
                            System.out.println("= = = = = = = = = =");
                        }
                    } else System.out.println("Waiting Queue is empty...");
                }

                case 0 -> {
                    System.out.println("Good-bye :]");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }
}
    