package com.az.gitember.service;

import com.az.gitember.GitemberApp;
import com.az.gitember.misc.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Igor_Azarny on 18 Dec 2016.
 */
public class SettingsServiceImpl {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final static Logger log = Logger.getLogger(SettingsServiceImpl.class.getName());

    private final static String SYSTEM_PROP_USER_HOME = "user.home";
    private final static String SYSTEM_PROP_OS_NAME = "os.name";

    private final static String OS = System.getProperty(SYSTEM_PROP_OS_NAME).toLowerCase();

    private GitemberSettings gitemberSettings;



    /**
     * Get home directory.
     *
     * @return home directory.
     */
    public String getUserHomeFolder() {
        return System.getProperty(SYSTEM_PROP_USER_HOME);
    }


    /**
     * Get absolute path path to property file with settings.
     *
     * @return
     * @throws IOException
     */
    private String getAbsolutePathToPropertyFile() throws IOException {
        return Files.createDirectories(
                Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME), Const.PROP_FOLDER)
        ).toFile().getAbsolutePath() + File.separator + Const.PROP_FILE_NAME;
    }



    /**
     * Get list of opened repositories.
     *
     * @return list of repo
     */
    public List<Pair<String, String>> getRecentProjects() {
        read();
        List<Pair<String, String>> pairs = new ArrayList<>();
        for(GitemberProjectSettings ps : gitemberSettings.getProjects()) {
            String folder = ps.getProjectHameFolder();
            Path projectHome = Paths.get(folder);
            if (Files.exists(projectHome)) {
                String shortName = new File(new File(folder).getParent()).getName();
                pairs.add(new Pair<>(shortName, folder));
            }
        }
        Collections.sort(pairs, Comparator.comparing(Pair::getFirst));
        return pairs;
    }
    /**
     * Read properties from disk.
     *
     * @return
     */
    private void read() { // todo each time or ??????
        File file = null;
        try {
            file = new File(getAbsolutePathToPropertyFile());
            GitemberSettings gitemberSettings =  objectMapper.readValue(file, GitemberSettings.class);
            gitemberSettings.getProjects().removeIf(
                    gitemberProjectSettings -> {
                        Path path = Paths.get(gitemberProjectSettings.getProjectHameFolder());
                        return  !path.toFile().exists();
                    }
            );
            this.gitemberSettings = gitemberSettings;
        } catch (IOException e) {
            this.gitemberSettings = new GitemberSettings();
            log.log(Level.SEVERE, "Sorry. Cannot read settings. New created.", e);
            if (file != null) {
                if (file.delete()) {
                    save();
                }
            }
        }

    }

    public Optional<GitemberProjectSettings> getRepositoryCred(String remoteUrl, String gitFolder) {
        return  gitemberSettings
                .getProjects()
                .stream()
                .filter(
                        ri -> ri.getProjectRemoteUrl().equalsIgnoreCase(remoteUrl)
                        && ri.getProjectHameFolder().equalsIgnoreCase(gitFolder)
                ).findFirst();
    }

    public void saveRepositoryCred(GitemberProjectSettings repoInfo) {
        gitemberSettings.getProjects().removeIf(
            gps -> {
                return gps.getProjectHameFolder().equals(repoInfo.getProjectHameFolder()) &&
                gps.getProjectRemoteUrl().equals(repoInfo.getProjectRemoteUrl());
            }

        );
        gitemberSettings.getProjects().add(repoInfo);
        save();
    }



    /**
     * Save properties.
     */
    public void save() {
        try {
            objectMapper.writerFor(GitemberSettings.class).writeValue(
                    new File(getAbsolutePathToPropertyFile()),
                    gitemberSettings
            );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }


    /*public void createNewGitemberProjectSettings(
            String userName,
            String userEMail,
            String projectRemoteUrl,
            String gitFolder
    ) {

        SettingsServiceImpl settingsSrv = Context.getSettingsService();

        GitemberProjectSettings ps = new GitemberProjectSettings();
        ps.setProjectName(gitFolder);
        ps.setProjectHameFolder(gitFolder);
        ps.setUserName(userName);
        ps.setUserEmail(userEMail);
        ps.setProjectRemoteUrl(projectRemoteUrl);

        //so just to try update  from saved creds and used it.
        //todo not sure what to upd

        Optional<GitemberProjectSettings> optRI = settingsSrv.getRepositoryCred(projectRemoteUrl, gitFolder);

        if (optRI.isPresent()) {
            ps.updateForLogon(optRI.get());
        }

        GitemberSettings gitemberSettings = Context.getSettingsService().getGitemberSettings();
        gitemberSettings.addGitemberProjectSettings(ps);
        gitemberSettings.setLastGitFolder(gitFolder);

        settingsSrv.save();
        GitemberApp.getGitemberService().setNewRepoInfo(ps);
    }*/


    //todo rename 4 f un

    /*public String getUserNameFromStoredRepoConfig() {
        return gitemberSettings.getLastProjectSettings().getUserName();
    }

    public String getUserEmailFromStoredRepoConfig() {
        return gitemberSettings.getLastProjectSettings().getUserEmail();
    }

    public String getRemoteUrlFromStoredRepoConfig() {
        return gitemberSettings.getLastProjectSettings().getProjectRemoteUrl();
    }*/

    public GitemberSettings getGitemberSettings() {
        return gitemberSettings;
    }
}
