/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.m2e.yang;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

import org.opendaylight.yangide.core.YangCorePlugin;
import org.opendaylight.yangide.core.YangModelException;
import org.opendaylight.yangide.core.indexing.IJob;
import org.opendaylight.yangide.core.indexing.JobAdapter;
import org.opendaylight.yangide.core.model.YangModelManager;
import org.opendaylight.yangide.core.parser.IYangValidationListener;
import org.opendaylight.yangide.core.parser.YangParserUtil;
import org.opendaylight.yangide.ui.YangUIPlugin;
import org.opendaylight.yangide.ui.preferences.YangPreferenceConstants;

/**
 * @author Konstantin Zaitsev
 * date: Jul 2, 2014
 */
public class YangBuildParticipant extends MojoExecutionBuildParticipant {

    public YangBuildParticipant(MojoExecution execution, boolean runOnIncremental) {
        super(execution, runOnIncremental);
    }

    @Override
    public Set<IProject> build(final int kind, IProgressMonitor monitor) throws Exception {
        long dt = System.currentTimeMillis();

        IMaven maven = MavenPlugin.getMaven();
        BuildContext buildContext = getBuildContext();
        String projectName = getMavenProjectFacade().getProject().getName();

        File source = maven.getMojoParameterValue(getSession().getCurrentProject(), getMojoExecution(),
                YangM2EPlugin.YANG_FILES_ROOT_DIR, File.class, monitor);
        if (source == null) {
            source = new File(getSession().getCurrentProject().getBasedir(), YangM2EPlugin.YANG_FILES_ROOT_DIR_DEFAULT);
        }
        Scanner ds = buildContext.newScanner(source);
        ds.scan();
        final String[] includedFiles = ds.getIncludedFiles();
        if (includedFiles == null || includedFiles.length <= 0) {
            return null;
        }
        // clear markers before build
        getMavenProjectFacade().getProject().deleteMarkers(YangCorePlugin.YANGIDE_PROBLEM_MARKER, true,
                IResource.DEPTH_INFINITE);

        Set<File> outputDirs = new HashSet<>();
        YangGeneratorConfiguration[] confs = maven.getMojoParameterValue(getSession().getCurrentProject(),
                getMojoExecution(), YangM2EPlugin.YANG_CODE_GENERATORS, YangGeneratorConfiguration[].class, monitor);
        if (confs != null) {
            for (YangGeneratorConfiguration conf : confs) {
                if (conf.getOutputBaseDir() != null) {
                    outputDirs.add(conf.getOutputBaseDir());
                }
            }
        }

        boolean isCleanRequired = YangUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(YangPreferenceConstants.M2E_PLUGIN_CLEAN_TARGET);

        if (isCleanRequired) {
            long dt2 = System.currentTimeMillis();
            for (File outputDir : outputDirs) {
                IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot()
                        .findContainersForLocationURI(URIUtil.toURI(outputDir.getAbsolutePath()));
                if (containers != null && containers.length > 0) {
                    containers[0].delete(true, monitor);
                }
            }
            YangM2EPlugin.traceTime(projectName, "clean classes", dt2, System.currentTimeMillis());
        }

        Set<IProject> result = super.build(kind, monitor);

        for (Throwable ex : getSession().getResult().getExceptions()) {
            YangCorePlugin.log(ex);
        }

        for (File outputDir : outputDirs) {
            buildContext.refresh(outputDir);
        }

        final IProject curProject = getMavenProjectFacade().getProject();
        curProject.touch(monitor);

        final File basedir = ds.getBasedir();

        // wait index job
        long dt3 = System.currentTimeMillis();
        if (kind == FULL_BUILD) {
            YangModelManager.getIndexManager().indexAll(curProject);
        }
        YangModelManager.getIndexManager().performConcurrentJob(new JobAdapter() {
            @Override
            public boolean execute(IProgressMonitor progress) {
                validate(basedir, includedFiles);
                return false;
            }
        }, IJob.WaitUntilReady, monitor);
        YangM2EPlugin.traceTime(projectName, "yang index", dt3, System.currentTimeMillis());

        YangM2EPlugin.traceTime(projectName, "total", dt, System.currentTimeMillis());
        return result;
    }

    private void validate(File basedir, String[] includedFiles) {
        for (String path : includedFiles) {
            final IFile ifile = YangCorePlugin.getIFileFromFile(new File(basedir, path));
            if (ifile != null) {
                try {
                    YangParserUtil.validateYangFile(YangCorePlugin.createYangFile(ifile).getBuffer().getContents()
                            .toCharArray(), ifile.getProject(), new IYangValidationListener() {

                        @Override
                        public void validationError(String msg, int lineNumber, int charStart, int charEnd) {
                            YangCorePlugin.createProblemMarker(ifile, msg, lineNumber, charStart, charEnd);
                        }

                        @Override
                        public void syntaxError(String msg, int lineNumber, int charStart, int charEnd) {
                            YangCorePlugin.createProblemMarker(ifile, msg, lineNumber, charStart, charEnd);
                        }
                    });
                } catch (YangModelException e) {
                    YangCorePlugin.log(e);
                }
            }
        }
    }
}
