package com.az.gitember.service;


import com.az.gitember.scm.impl.git.GitRepositoryService;

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
    private static GitemberServiceImpl gitemberService;

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
