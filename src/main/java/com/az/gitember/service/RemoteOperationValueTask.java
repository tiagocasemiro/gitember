package com.az.gitember.service;

import com.az.gitember.misc.Result;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import javafx.concurrent.Task;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.function.Supplier;

public class RemoteOperationValueTask   extends Task<Result>  {

    private  Supplier<Result> supplier;
    private  GitemberServiceImpl service;

    private final ProgressMonitor progressMonitor = new DefaultProgressMonitor((t, d) -> {
        updateTitle(t);
        updateProgress(d, 1.0);
    });

    public RemoteOperationValueTask(final GitemberServiceImpl service, Supplier<Result> supplier) {
        this.service = service;
        this.supplier = supplier;
    }

    public RemoteOperationValueTask(GitemberServiceImpl service) {
        this.service = service;
    }

    public RemoteOperationValueTask() {
    }

    public Supplier<Result> getSupplier() {
        return supplier;
    }

    public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void setSupplier(Supplier<Result> supplier) {
        this.supplier = supplier;
    }

    public void setService(GitemberServiceImpl service) {
        this.service = service;
    }

    @Override
    protected Result call() throws Exception {
        return service.remoteRepositoryOperation(
                supplier
        );
    }


}
