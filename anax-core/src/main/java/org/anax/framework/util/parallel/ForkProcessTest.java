//package org.anax.framework.util.parallel;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.io.Serializable;
//import java.util.Arrays;
//import java.util.List;
//@Slf4j
//public class ForkProcessTest implements Serializable {
//
//    ForkClient client;
//
//    public ForkProcessTest() throws IOException {
//        List<String> debugCMD = Arrays.asList("java", "-agentlib:jdwp=transport=dt_socket,server=n,address=tangelatos-mbp.lan:5005,suspend=y");
//        List<String> normalCMD = Arrays.asList("java", "-Xmx512m");
//        client = new ForkClient(this.getClass().getClassLoader(), this, normalCMD, TimeoutLimits.DEFAULTS );
//    }
//
//    public void helloWorld(String something) throws Exception {
//        log.info("{} >>> Hello World " + something + " !!!!", pid());
//    }
//
//    public void go() throws Exception {
//        log.info("{} --- Calling hello world", pid());
//        client.call("helloWorld", "THANOS");
//    }
//
//    public static void main(String[] args) throws Exception {
//        ForkProcessTest fpt = new ForkProcessTest();
//        fpt.go();
//    }
//
//    public int pid () throws Exception {
//        java.lang.management.RuntimeMXBean runtime =
//                java.lang.management.ManagementFactory.getRuntimeMXBean();
//        java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
//        jvm.setAccessible(true);
//        sun.management.VMManagement mgmt =
//                (sun.management.VMManagement) jvm.get(runtime);
//        java.lang.reflect.Method pid_method =
//                mgmt.getClass().getDeclaredMethod("getProcessId");
//        pid_method.setAccessible(true);
//
//        int pid = (Integer) pid_method.invoke(mgmt);
//        return pid;
//    }
//
//}
