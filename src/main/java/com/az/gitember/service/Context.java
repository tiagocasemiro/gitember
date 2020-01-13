package com.az.gitember.service;


import com.az.gitember.scm.impl.git.GitRepositoryService;
import org.apache.commons.io.FileUtils;

import java.io.IOException;

/**
 *
 * Simple application context. Dont need to have complexity of spring.
 *
 */
public class Context {

    //todo
    private static GitRepositoryService repositoryService = new GitRepositoryService();
    private static SettingsServiceImpl settingsService = new SettingsServiceImpl();
    // just for create or clone
    private static GitemberServiceImpl gitemberService = new GitemberServiceImpl(new GitRepositoryService());

    /*
    MainViewController controller = fxmlLoader.getController();
    gitemberService.setProgressBar(controller.progressBar);
            gitemberService.setOperationProgressBar(controller.operationProgressBar);
            gitemberService.setOperationName(controller.operationName);*/

    public static void initContext(String gitFolder) throws IOException {
        repositoryService = new GitRepositoryService(gitFolder);
        gitemberService = new GitemberServiceImpl(repositoryService);
    }

    public static SettingsServiceImpl getSettingsService() {
        return settingsService;
    }

    public static GitemberServiceImpl getGitemberService() {
        return gitemberService;
    }
}
