package com.az.gitember.scm.impl.git;


import com.az.gitember.misc.CommitInfo;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.scm.exception.GECannotDeleteCurrentBranchException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.Assert.*;

public class GitRepositoryServiceTest {

    private static String README_FILE = "README.md";
    private static String IGNORE_FILE = ".gitignore";
    private static String FOLDER = "folder";
    private static String MORE_FILE1 = "file1.txt";
    private static String FN_BR1 = "refs/heads/br1";
    private static String FN_MASTER = "refs/heads/master";

    private String tmpGitProject = null;
    private Path tmpGitProjectPath = null;
    private GitRepositoryService gitRepositoryService;


    @Before
    public void setUp() throws Exception {
        tmpGitProjectPath = Files.createTempDirectory("tmpgitember");
        tmpGitProject = tmpGitProjectPath.toAbsolutePath().toString();
        GitRepositoryService.createRepository(tmpGitProject);
        gitRepositoryService = new GitRepositoryService(
                Paths.get(tmpGitProject , ".git").toString()
        );
        System.out.println("Temp project folder is " + tmpGitProject);

    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File(tmpGitProject));
    }

    @Test
    public void createRepository() throws Exception {
        assertTrue(Files.exists(Paths.get(tmpGitProjectPath.toString(), README_FILE)));
        assertTrue(Files.exists(Paths.get(tmpGitProjectPath.toString(), IGNORE_FILE)));
    }

    @Test
    public void addFileToCommitStage() throws Exception {
        DirCache dc = gitRepositoryService.addFileToCommitStage(README_FILE);
        assertEquals(1, dc.getEntryCount());

        Files.createDirectories(Paths.get(tmpGitProjectPath.toString(), FOLDER));
        Files.write(
                Paths.get(tmpGitProjectPath.toString(), FOLDER, MORE_FILE1),
                "test".getBytes(), StandardOpenOption.CREATE);
        dc = gitRepositoryService.addFileToCommitStage(
                Paths.get(FOLDER).toString()
        );

        assertNotNull(dc);
        assertEquals(2, dc.getEntryCount());
    }

    @Test
    public void addFileToCommitStageNegative() throws Exception {
        DirCache dc = gitRepositoryService.addFileToCommitStage("NotExistingFile.txt");
        assertEquals(0, dc.getEntryCount());
    }

    @Test
    public void removeFileFromCommitStage()  throws Exception {
        DirCache dc = gitRepositoryService.addFileToCommitStage(README_FILE);
        assertEquals(1, dc.getEntryCount());

        Ref ref = gitRepositoryService.removeFileFromCommitStage(README_FILE);
        assertNotNull(ref);
    }

    @Test
    public void commit()  throws Exception {
        DirCache dc = gitRepositoryService.addFileToCommitStage(README_FILE);
        assertEquals(1, dc.getEntryCount());
        dc = gitRepositoryService.addFileToCommitStage(IGNORE_FILE);
        assertEquals(2, dc.getEntryCount());

        RevCommit rc = gitRepositoryService.commit("Some commit msg");
        assertEquals("Some commit msg", rc.getShortMessage());
    }

    @Test
    public void getAllFiles()  throws Exception {
        assertEquals(0, gitRepositoryService.getAllFiles().size());
        commit();
        assertEquals(2, gitRepositoryService.getAllFiles().size());
    }


    @Test
    public void createLocalBranch() throws Exception {
        commit();
        Ref newBranch = gitRepositoryService.createLocalBranch(FN_MASTER, "br1");
        gitRepositoryService.checkoutLocalBranch(FN_BR1);
        assertEquals(FN_BR1, newBranch.getName());
        assertEquals(2, gitRepositoryService.getAllFiles(FN_BR1).size());

        // adding new file
        Files.createDirectories(Paths.get(tmpGitProjectPath.toString(), FOLDER));
        Files.write(
                Paths.get(tmpGitProjectPath.toString(), FOLDER, MORE_FILE1),
                "test".getBytes(), StandardOpenOption.CREATE);
        DirCache dc = gitRepositoryService.addFileToCommitStage(
                Paths.get(FOLDER).toString()
        );
        RevCommit rc = gitRepositoryService.commit("Added new file to br1");
        assertEquals("Added new file to br1",rc.getShortMessage());

        Ref masterRef = gitRepositoryService.checkoutLocalBranch(FN_MASTER);
        assertEquals(2, gitRepositoryService.getAllFiles(FN_MASTER).size());

        Ref br1Ref = gitRepositoryService.checkoutLocalBranch(FN_BR1);
        assertEquals(3, gitRepositoryService.getAllFiles(FN_BR1).size());
    }


    @Test(expected = GECannotDeleteCurrentBranchException.class)
    public void deleteLocalBranchNegative() throws Exception {
        createLocalBranch();
        gitRepositoryService.deleteLocalBranch(FN_BR1);
    }

    @Test
    public void deleteLocalBranch() throws Exception {
        createLocalBranch();
        gitRepositoryService.checkoutLocalBranch(FN_MASTER);
        gitRepositoryService.deleteLocalBranch(FN_BR1);
    }

    @Test
    public void mergeLocalBranch() throws Exception {
        createLocalBranch();
        gitRepositoryService.checkoutLocalBranch(FN_MASTER);
        assertEquals(2, gitRepositoryService.getAllFiles(FN_MASTER).size());
        gitRepositoryService.mergeLocalBranch(FN_BR1, "Merger from branch 1");
        assertEquals(3, gitRepositoryService.getAllFiles(FN_MASTER).size());
    }

    @Test
    public void getLocalBranches() throws Exception {
        createLocalBranch();
        assertEquals(2, gitRepositoryService.getLocalBranches().size());
        gitRepositoryService.checkoutLocalBranch(FN_MASTER);
        gitRepositoryService.deleteLocalBranch(FN_BR1);
        assertEquals(1, gitRepositoryService.getLocalBranches().size());
    }

    @Test
    public void getFileHistory() throws Exception {
        commit();
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        gitRepositoryService.addFileToCommitStage(README_FILE);
        gitRepositoryService.commit("Changes in read me");
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n Bender".getBytes(), StandardOpenOption.APPEND);
        Files.write(Paths.get(tmpGitProject, IGNORE_FILE),
                "\n Bender".getBytes(), StandardOpenOption.APPEND);

        gitRepositoryService.addFileToCommitStage(README_FILE);
        gitRepositoryService.addFileToCommitStage(IGNORE_FILE);
        gitRepositoryService.commit("Benders added in read me");

        List<ScmRevisionInformation> lst = gitRepositoryService.getFileHistory(FN_MASTER, README_FILE);
        assertEquals(3, lst.size());

        assertEquals("Benders added in read me", lst.get(0).getShortMessage());
        assertEquals("Changes in read me", lst.get(1).getShortMessage());
        assertEquals("Some commit msg", lst.get(2).getShortMessage());
    }


    @Test
    public void createTag() throws Exception {
        commit();
        gitRepositoryService.createTag("t0");
        gitRepositoryService.createTag("t00");
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        gitRepositoryService.addFileToCommitStage(README_FILE);
        gitRepositoryService.commit("Changes in read me");
        gitRepositoryService.createTag("t1");
        List<ScmBranch> lst = gitRepositoryService.getTags();
        assertEquals(3, lst.size());
        assertEquals("refs/tags/t0", lst.get(0).getShortName());
        assertEquals("refs/tags/t00", lst.get(1).getShortName());
        assertEquals("refs/tags/t1", lst.get(2).getShortName());
    }


    @Test
    public void getHead() throws Exception {
        commit();
        CommitInfo ci = gitRepositoryService.getHead();
        assertNotNull(ci);
        assertEquals("refs/heads/master", ci.getName());
    }





    @Test
    public void stash() throws Exception {
        commit();
        Path readmePath = Paths.get(tmpGitProject, README_FILE);
        List<ScmRevisionInformation> lst = gitRepositoryService.getStashList();
        assertEquals(0, lst.size());

        byte[] readmeBytesOriginal = Files.readAllBytes(readmePath);
        Files.write(readmePath,
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged0 = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged0.length);
        gitRepositoryService.stash();
        byte[] readmeBytesStashed = Files.readAllBytes(readmePath);
        assertEquals(readmeBytesOriginal.length, readmeBytesStashed.length);

        Files.write(readmePath,
                "\n readme changes 1".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged1 = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged1.length);
        gitRepositoryService.stash();

        lst = gitRepositoryService.getStashList();
        assertEquals(2, lst.size());


        gitRepositoryService.applyStash(lst.get(0).getRevisionFullName());
        byte[] readmeBytesApplied = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesApplied.length);

        gitRepositoryService.deleteStash(0);
        lst = gitRepositoryService.getStashList();
        assertEquals(1, lst.size());

    }


    @Test
    public void checkoutFile() throws Exception {
        commit();
        Path readmePath = Paths.get(tmpGitProject, README_FILE);

        byte[] readmeBytesOriginal = Files.readAllBytes(readmePath);
        Files.write(readmePath,
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged = Files.readAllBytes(readmePath);

        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged.length);
        gitRepositoryService.checkoutFile(README_FILE, null);

        byte[] readmeBytesOriginalNew = Files.readAllBytes(readmePath);

        assertEquals(readmeBytesOriginal.length, readmeBytesOriginalNew.length);
    }












}
