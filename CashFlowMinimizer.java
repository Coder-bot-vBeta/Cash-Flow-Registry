import java.util.*;

class Bank {
    String name;
    int netAmount;
    Set<String> types;

    Bank(String name) {
        this.name = name;
        this.netAmount = 0;
        this.types = new HashSet<>();
    }
}

public class CashFlowMinimizer {
    public static void main(String[] args) {
        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in the different corners of the world that use different modes of payment. There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment. \n\n");
        System.out.println("Enter the number of banks participating in the transactions.");
        
        Scanner sc = new Scanner(System.in);
        int numBanks = sc.nextInt();
        
        Bank[] banks = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();
        
        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces");
        
        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            if (i == 0) {
                System.out.print("World Bank : ");
            } else {
                System.out.print("Bank " + i + " : ");
            }
            String name = sc.next();
            banks[i] = new Bank(name);
            indexOf.put(name, i);
            
            int numTypes = sc.nextInt();
            if (i == 0) {
                maxNumTypes = numTypes;
            }
            
            for (int j = 0; j < numTypes; j++) {
                String type = sc.next();
                banks[i].types.add(type);
            }
        }
        
        System.out.println("Enter number of transactions.");
        int numTransactions = sc.nextInt();
        
        int[][] graph = new int[numBanks][numBanks];
        
        System.out.println("Enter the details of each transaction as stated:");
        System.out.println("Debtor Bank, creditor Bank and amount");
        System.out.println("The transactions can be in any order");
        for (int i = 0; i < numTransactions; i++) {
            System.out.print(i + " th transaction : ");
            String debtor = sc.next();
            String creditor = sc.next();
            int amount = sc.nextInt();
            
            graph[indexOf.get(debtor)][indexOf.get(creditor)] = amount;
        }
        
        minimizeCashFlow(numBanks, banks, indexOf, numTransactions, graph, maxNumTypes);
        sc.close();
    }

    static int getMinIndex(Bank[] listOfNetAmounts) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    static int getSimpleMaxIndex(Bank[] listOfNetAmounts) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    static Pair getMaxIndex(Bank[] listOfNetAmounts, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < 0) continue;

            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }

        return new Pair(maxIndex, matchingType);
    }

    static void printAns(List<List<Pair>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows : \n\n");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                if (ansGraph.get(i).get(j).first != 0 && ansGraph.get(j).get(i).first != 0) {
                    if (ansGraph.get(i).get(j).first == ansGraph.get(j).get(i).first) {
                        ansGraph.get(i).get(j).first = 0;
                        ansGraph.get(j).get(i).first = 0;
                    } else if (ansGraph.get(i).get(j).first > ansGraph.get(j).get(i).first) {
                        ansGraph.get(i).get(j).first -= ansGraph.get(j).get(i).first;
                        ansGraph.get(j).get(i).first = 0;

                        System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).first + " to " + input[j].name + " via " + ansGraph.get(i).get(j).second);
                    } else {
                        ansGraph.get(j).get(i).first -= ansGraph.get(i).get(j).first;
                        ansGraph.get(i).get(j).first = 0;

                        System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).first + " to " + input[i].name + " via " + ansGraph.get(j).get(i).second);
                    }
                } else if (ansGraph.get(i).get(j).first != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).first + " to " + input[j].name + " via " + ansGraph.get(i).get(j).second);
                } else if (ansGraph.get(j).get(i).first != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).first + " to " + input[i].name + " via " + ansGraph.get(j).get(i).second);
                }

                ansGraph.get(i).get(j).first = 0;
                ansGraph.get(j).get(i).first = 0;
            }
        }
        System.out.println("\n");
    }

    static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        
        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank(input[b].name);
            listOfNetAmounts[b].types = input[b].types;
            
            int amount = 0;
            for (int i = 0; i < numBanks; i++) {
                amount += graph[i][b];
            }
            for (int j = 0; j < numBanks; j++) {
                amount -= graph[b][j];
            }
            
            listOfNetAmounts[b].netAmount = amount;
        }

        List<List<Pair>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            List<Pair> row = new ArrayList<>();
            for (int j = 0; j < numBanks; j++) {
                row.add(new Pair(0, ""));
            }
            ansGraph.add(row);
        }

        int numZeroNetAmounts = 0;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) numZeroNetAmounts++;
        }
        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts);
            Pair maxAns = getMaxIndex(listOfNetAmounts, minIndex, input, maxNumTypes);

            int maxIndex = maxAns.first;
            if (maxIndex == -1) {
                ansGraph.get(minIndex).get(0).first += Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(minIndex).get(0).second = input[minIndex].types.iterator().next();

                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts);
                ansGraph.get(0).get(simpleMaxIndex).first += Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(0).get(simpleMaxIndex).second = input[simpleMaxIndex].types.iterator().next();

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);

                ansGraph.get(minIndex).get(maxIndex).first += transactionAmount;
                ansGraph.get(minIndex).get(maxIndex).second = maxAns.second;

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    static class Pair {
        int first;
        String second;

        Pair(int first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
