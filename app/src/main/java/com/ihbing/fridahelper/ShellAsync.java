package com.ihbing.fridahelper;

import com.ihbing.fridahelper.go.Go;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellAsync {
    //静态数据
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    //输入数据
    private String commands[];
    private boolean isRoot;
    private volatile Status status;
    private OnShellAsyncListener onShellAsyncListener;
    //内部数据
    Process process = null;
    BufferedReader successResult = null;
    BufferedReader errorResult = null;
    StringBuilder successMsg = null;
    StringBuilder errorMsg = null;

    DataOutputStream os = null;

    public ShellAsync(String commands[], boolean isRoot, OnShellAsyncListener onShellAsyncListener) {
        this.commands = commands;
        this.isRoot = isRoot;
        this.onShellAsyncListener = onShellAsyncListener;
        this.status = Status.BEFORE_RUNNING;
    }

    public ShellAsync(String command, boolean isRoot, OnShellAsyncListener onShellAsyncListener) {
        this.commands = new String[]{command};
        this.isRoot = isRoot;
        this.onShellAsyncListener = onShellAsyncListener;
    }

    public void start() {
        if (status == Status.RUNNING) return;
        if (commands == null || commands.length == 0) {
            return;
        }
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }

            os.writeBytes(COMMAND_EXIT);

            os.flush();

            status = Status.RUNNING;
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            while (status == Status.RUNNING) {

                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));

                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));

//                if(!(null!=successResult|null!=errorResult))continue;

                String s;

                while (status == Status.RUNNING && (s = successResult.readLine()) != null) {

                    successMsg.append(s).append("\n");

                    if (null == onShellAsyncListener) continue;

                    onShellAsyncListener.onReadLine(s);

                }

                while (status == Status.RUNNING && (s = errorResult.readLine()) != null) {

                    errorMsg.append(s).append("\n");

                    if (null == onShellAsyncListener) continue;

                    onShellAsyncListener.onReadLine(s);

                }

                Thread.sleep(500);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (status == Status.RUNNING) {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (process != null) {
                    process.destroy();
                }
                status = Status.AFTER_RUNNING;
            }
        }
        if (onShellAsyncListener != null) {
            onShellAsyncListener.onStopped();
        }
    }

    public void stop() {
        Go.go(()->{
//            try {
//                if (os != null) {
//                    os.close();
//                }
//                if (successResult != null) {
//                    successResult.close();
//                }
//                if (errorResult != null) {
//                    errorResult.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            if (process != null) {
                process.destroy();
            }
            status = Status.AFTER_RUNNING;
        });

    }

    public interface OnShellAsyncListener {
        void onReadLine(String line);

        void onStopped();
    }

    private static enum Status {
        BEFORE_RUNNING,
        RUNNING,
        AFTER_RUNNING

    }
}
