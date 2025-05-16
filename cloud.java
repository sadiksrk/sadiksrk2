import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

public class CloudSimExample1 {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;

    public static void main(String[] args) {

        Log.printLine("Starting CloudSimExample1...");

        try {
            // Initialize CloudSim
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUser, calendar, traceFlag);

            // Create Datacenter and Broker
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Create VMs
            vmlist = new ArrayList<>();
            int vmid = 0;
            int mips = 1000;
            long size = 10000;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm1 = new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm2 = new Vm(vmid++, brokerId, mips * 2, pesNumber, ram - 256, bw, size * 2, vmm, new CloudletSchedulerTimeShared());
            Vm vm3 = new Vm(vmid++, brokerId, mips / 2, pesNumber, ram + 256, bw, size * 3, vmm, new CloudletSchedulerTimeShared());
            Vm vm4 = new Vm(vmid++, brokerId, mips * 4, pesNumber, ram, bw, size * 4, vmm, new CloudletSchedulerTimeShared());

            vmlist.add(vm1);
            vmlist.add(vm2);
            vmlist.add(vm3);
            vmlist.add(vm4);

            broker.submitVmList(vmlist);

            // Create Cloudlets
            cloudletList = new ArrayList<>();
            int id = 0;
            long length = 400000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet1 = new Cloudlet(id++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet2 = new Cloudlet(id++, length * 2, pesNumber, fileSize * 2, outputSize / 3, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet3 = new Cloudlet(id++, length / 2, pesNumber, fileSize * 3, outputSize * 3, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet4 = new Cloudlet(id++, length / 3, pesNumber, fileSize / 3, outputSize / 2, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet5 = new Cloudlet(id++, length * 3, pesNumber, fileSize / 2, outputSize / 4, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet6 = new Cloudlet(id++, length / 4, pesNumber, fileSize * 4, outputSize * 4, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet7 = new Cloudlet(id++, length * 4, pesNumber, fileSize, outputSize * 2, utilizationModel, utilizationModel, utilizationModel);
            Cloudlet cloudlet8 = new Cloudlet(id++, length, pesNumber, fileSize / 4, outputSize / 3, utilizationModel, utilizationModel, utilizationModel);

            cloudlet1.setUserId(brokerId);
            cloudlet2.setUserId(brokerId);
            cloudlet3.setUserId(brokerId);
            cloudlet4.setUserId(brokerId);
            cloudlet5.setUserId(brokerId);
            cloudlet6.setUserId(brokerId);
            cloudlet7.setUserId(brokerId);
            cloudlet8.setUserId(brokerId);

            cloudletList.addAll(Arrays.asList(cloudlet1, cloudlet2, cloudlet3, cloudlet4, cloudlet5, cloudlet6, cloudlet7, cloudlet8));
            broker.submitCloudletList(cloudletList);

            // Optional manual cloudlet binding
            broker.bindCloudletToVm(cloudlet1.getCloudletId(), vm1.getId());
            broker.bindCloudletToVm(cloudlet2.getCloudletId(), vm2.getId());
            broker.bindCloudletToVm(cloudlet3.getCloudletId(), vm3.getId());
            broker.bindCloudletToVm(cloudlet4.getCloudletId(), vm4.getId());
            broker.bindCloudletToVm(cloudlet5.getCloudletId(), vm1.getId());
            broker.bindCloudletToVm(cloudlet6.getCloudletId(), vm2.getId());
            broker.bindCloudletToVm(cloudlet7.getCloudletId(), vm3.getId());
            broker.bindCloudletToVm(cloudlet8.getCloudletId(), vm4.getId());

            // Run the simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            // Print results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("CloudSimExample1 finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened.");
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone,
                cost, costPerMem, costPerStorage, costPerBw
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        Log.printLine("\n========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Datacenter ID" + indent + "VM ID" + indent + "Time" +
                indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent +
                        indent + cloudlet.getVmId() + indent +
                        indent + dft.format(cloudlet.getActualCPUTime()) + indent +
                        indent + dft.format(cloudlet.getExecStartTime()) + indent +
                        indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}

Overview of the code
---Initialize the cloud sim enviornment
---create data center with host
---create a broker which as a mediator between user and data center
---define and create four virtual machine
---define and create cloudleds
---bind specific cloudlet to the virtual machine
---starting simulation
---print result

This will initialize the simulation
Num_users.number and cloud users
Calender.system time
Trace_files

Open cpu with 1000mips
1000 bandwidth
Manage vm and cloud lites :
Create Broker DatacenterBroker broker = createBroker(); int brokerId = broker.getId();

