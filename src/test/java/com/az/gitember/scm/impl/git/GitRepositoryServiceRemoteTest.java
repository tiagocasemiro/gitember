package com.az.gitember.scm.impl.git;


import com.az.gitember.misc.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.SystemReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class GitRepositoryServiceRemoteTest {

    private static String MORE_FILE0 = "file0.txt";
    private static String README_FILE = "README.md";
    private static String IGNORE_FILE = ".gitignore";

    private static String RN_RBR1 = "refs/remotes/origin/rbr1";
    private static String FN_RBR1 = "refs/heads/rbr1";
    private static String FN_MASTER = "refs/heads/master";

    private String remoteGitProject = null;
    private Path remoteGitProjectPath = null;
    private GitRepositoryService remoteRepositoryService;

    private SimpleHttpServer simpleHttpServer;

    private String clonedRepoPath;
    private GitRepositoryService clonedRepositoryService;

    private String fromRepo;
    private String defaultUser = "agitter";
    private String defaultPassword = "letmein";


    @Before
    public void setUp() throws Exception {
        remoteGitProjectPath = Files.createTempDirectory("gitember-remote");
        remoteGitProject = remoteGitProjectPath.toAbsolutePath().toString();
        GitRepositoryService.createRepository(remoteGitProject);
        remoteRepositoryService = new GitRepositoryService(
                Paths.get(remoteGitProject, ".git").toString()
        );
        remoteRepositoryService.addFileToCommitStage(README_FILE);
        remoteRepositoryService.addFileToCommitStage(IGNORE_FILE);
        remoteRepositoryService.commit("Remote commit 1");

        simpleHttpServer = new SimpleHttpServer(remoteRepositoryService.getRepository());
        simpleHttpServer.start();

        clonedRepoPath = Files.createTempDirectory("gitember-cloned").toString();
        clonedRepositoryService = new GitRepositoryService(  Paths.get(clonedRepoPath, ".git").toString() );
        fromRepo = simpleHttpServer.getUri().toString();
    }

    @After
    public void tearDown() throws Exception {
        simpleHttpServer.stop();
        remoteRepositoryService.shutdown();
        clonedRepositoryService.shutdown();
        FileUtils.deleteDirectory(new File(remoteGitProject));
        FileUtils.deleteDirectory(new File(clonedRepoPath));
    }

    @Test
    public void testClone() throws Exception {
        remoteRepositoryService.cloneRepository(
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
        simpleHttpServer = new SimpleHttpServer(remoteRepositoryService.getRepository(), true);
        simpleHttpServer.start();


        // just to be sure that service code will change the value to false
        StoredConfig fbcOrig = SystemReader.getInstance().getUserConfig();
        fbcOrig.setBoolean(Const.Config.HTTP, null, Const.Config.SLL_VERIFY, true);
        fbcOrig.save();


        fromRepo = simpleHttpServer.getSecureUri().toString();

        try {

            remoteRepositoryService.cloneRepository(
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
        simpleHttpServer = new SimpleHttpServer(remoteRepositoryService.getRepository(), true);
        simpleHttpServer.start();

        fromRepo = simpleHttpServer.getSecureUri().toString();

        try {
            remoteRepositoryService.cloneRepository(
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
            remoteRepositoryService.cloneRepository(
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
            remoteRepositoryService.cloneRepository(
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


    private static Object[] fetchParams() {
        return new Object[]{
                //new Object[]{null, 3},
                new Object[]{"refs/heads/rbr1", 3},
        };
    }

    @Parameters(method = "fetchParams")
    @Test
    public void testRemoteRepositoryFetch(final String remoteBranch, final int expectedFiles) throws Exception {

        testRemoteRepositoryPush();

        Result rez = clonedRepositoryService.remoteRepositoryFetch(
                remoteBranch,
                defaultUser,defaultPassword,
                null
        );

        assertEquals(Result.Code.OK, rez.getCode());
        assertEquals(expectedFiles, clonedRepositoryService.getAllFiles(RN_RBR1).size());


        //"refs/remotes/origin/rbr1";
        //private static String FN_RBR1 = "refs/heads/rbr1";

                /*



        //all "+refs/heads/*","refs/heads/*"
        specs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
            specs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
            specs.add(new RefSpec("+refs/notes/*:refs/notes/*"));

        //assertTrue(false);
*/

    }

    @Test
    public void testRemoteRepositoryPush() throws Exception {

        testClone();

        Files.write(Paths.get(clonedRepoPath, MORE_FILE0),
                "test file 0 to br1".getBytes(), StandardOpenOption.CREATE);

        DirCache dc = clonedRepositoryService.addFileToCommitStage( Paths.get(MORE_FILE0).toString() );

        clonedRepositoryService.commit("File 0 added");

        //local master to remote rbr1
        RefSpec refSpec = new RefSpec(FN_MASTER + ":" + FN_RBR1);

        Result rez = clonedRepositoryService.remoteRepositoryPush(
                refSpec,
                defaultUser, defaultPassword,
                null

        );
        assertEquals(Result.Code.OK, rez.getCode());
        assertEquals(3, remoteRepositoryService.getAllFiles(FN_RBR1).size());



    }


}
