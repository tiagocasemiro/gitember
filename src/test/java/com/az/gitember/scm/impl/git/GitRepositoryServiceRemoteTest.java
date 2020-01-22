package com.az.gitember.scm.impl.git;


import com.az.gitember.misc.*;
import com.az.gitember.scm.exception.GECannotDeleteCurrentBranchException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.Assert.*;

public class GitRepositoryServiceRemoteTest {

    private static String MORE_FILE0 = "file0.txt";
    private static String README_FILE = "README.md";
    private static String IGNORE_FILE = ".gitignore";

    private static String FN_BR1 = "refs/heads/br1";
    private static String FN_MASTER = "refs/heads/master";

    private String tmpGitProject = null;
    private Path tmpGitProjectPath = null;
    private GitRepositoryService gitRepositoryService;

    private SimpleHttpServer simpleHttpServer;

    private String clonedRepoPath;
    private String fromRepo;
    private String defaultUser = "agitter";
    private String defaultPassword = "letmein";


    @Before
    public void setUp() throws Exception {
        tmpGitProjectPath = Files.createTempDirectory("tmpgitember");
        tmpGitProject = tmpGitProjectPath.toAbsolutePath().toString();
        GitRepositoryService.createRepository(tmpGitProject);
        gitRepositoryService = new GitRepositoryService(
                Paths.get(tmpGitProject, ".git").toString()
        );
        gitRepositoryService.addFileToCommitStage(README_FILE);
        gitRepositoryService.addFileToCommitStage(IGNORE_FILE);
        gitRepositoryService.commit("Remote commit 1");

        simpleHttpServer = new SimpleHttpServer(gitRepositoryService.getRepository());
        simpleHttpServer.start();

        clonedRepoPath = Files.createTempDirectory("gitember-cloned").toString();
        fromRepo = simpleHttpServer.getUri().toString();
    }

    @After
    public void tearDown() throws Exception {
        simpleHttpServer.stop();
        FileUtils.deleteDirectory(new File(tmpGitProject));
        FileUtils.deleteDirectory(new File(clonedRepoPath));
    }

    @Test
    public void testClone() throws Exception {
        gitRepositoryService.cloneRepository(
                fromRepo,
                clonedRepoPath,
                defaultUser, defaultPassword,
                null, null
        );

        assertTrue(Files.exists(Paths.get(clonedRepoPath, README_FILE)));
        assertTrue(Files.exists(Paths.get(clonedRepoPath, IGNORE_FILE)));
    }

    @Test
    public void testCloneHTTPS() throws Exception {

        // restart http server with https support
        simpleHttpServer.stop();
        simpleHttpServer = new SimpleHttpServer(gitRepositoryService.getRepository(), true);
        simpleHttpServer.start();


        // just to be sure that service code will change the value to false
        StoredConfig fbcOrig = SystemReader.getInstance().getUserConfig();
        fbcOrig.setBoolean(Const.Config.HTTP, null, Const.Config.SLL_VERIFY, true);
        fbcOrig.save();


        fromRepo = simpleHttpServer.getSecureUri().toString();

        try {

            gitRepositoryService.cloneRepository(
                    fromRepo,
                    clonedRepoPath,
                    defaultUser, defaultPassword,
                    null, null
            );

            assertTrue(Files.exists(Paths.get(clonedRepoPath, README_FILE)));
            assertTrue(Files.exists(Paths.get(clonedRepoPath, IGNORE_FILE)));
        } catch (Exception e) {
            assertEquals("Not, expected", e.getMessage());
        }


    }

    @Test
    public void testCloneHTTPSBadHandshake() throws Exception {

        simpleHttpServer.stop();
        simpleHttpServer = new SimpleHttpServer(gitRepositoryService.getRepository(), true);
        simpleHttpServer.start();

        fromRepo = simpleHttpServer.getSecureUri().toString();

        try {
            gitRepositoryService.cloneRepository(
                    fromRepo,
                    clonedRepoPath,
                    defaultUser, defaultPassword,
                    null, null
            );
        } catch (TransportException te) {
            assertTrue(te.getMessage().contains(Const.Msg.TRANSPORT_SSL_ISSUE));
        } catch (GitAPIException e) {
            assertEquals("Not, expected", e.getMessage());
        }
    }

    @Test
    public void testCloneNoCredentialsProvider() {
        try {
            gitRepositoryService.cloneRepository(
                    fromRepo,
                    clonedRepoPath,
                    null, null,
                    null, null
            );
            assertTrue(false);
        } catch (TransportException te) {
            assertTrue(te.getMessage().contains(Const.Msg.TRANSPORT_CRED_PROVIDER_ISSUE));
        } catch (Exception e) {
            assertEquals("Not, expected", e.getMessage());
        }
    }


    @Test
    public void testCloneNoWrongCredentials() {
        try {
            gitRepositoryService.cloneRepository(
                    fromRepo,
                    clonedRepoPath,
                    "any", "wrong",
                    null, null
            );
            assertTrue(false);
        } catch (TransportException te) {
            assertTrue(te.getMessage().contains(Const.Msg.TRANSPORT_CRED_WRONG_ISSUE));
        } catch (Exception e) {
            assertEquals("Not, expected", e.getMessage());
        }
    }

    @Test
    public void testRemoteRepositoryPush() throws Exception {

        testClone();

        Files.write(Paths.get(clonedRepoPath, MORE_FILE0),
                "test file 0 to br1".getBytes(), StandardOpenOption.CREATE);

        GitRepositoryService cloned = new GitRepositoryService(
                Paths.get(tmpGitProject, ".git").toString()
        );

        cloned.addFileToCommitStage(
                Paths.get(MORE_FILE0).toString()
        );

        cloned.commit("File 0 added");

        cloned.remoteRepositoryPush(

        )

    }


}
