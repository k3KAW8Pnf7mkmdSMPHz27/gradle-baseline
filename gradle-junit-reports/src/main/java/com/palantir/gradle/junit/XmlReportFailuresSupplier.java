/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.gradle.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;

public final class XmlReportFailuresSupplier implements FailuresSupplier {

    public static <T extends Task & Reporting<? extends ReportContainer<SingleFileReport>>>
            XmlReportFailuresSupplier create(final T task, final ReportHandler<T> reportHandler) {
        // Ensure any necessary output is enabled
        task.getProject().afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project _project) {
                reportHandler.configureTask(task);
            }
        });

        return new XmlReportFailuresSupplier(task, reportHandler);
    }

    private final Reporting<? extends ReportContainer<SingleFileReport>> reporting;
    private final ReportHandler<?> reportHandler;

    private XmlReportFailuresSupplier(
            Reporting<? extends ReportContainer<SingleFileReport>> reporting, ReportHandler<?> reportHandler) {
        this.reporting = reporting;
        this.reportHandler = reportHandler;
    }

    @Override
    public List<Failure> getFailures() throws IOException {
        File sourceReport = Optional.ofNullable(reporting.getReports().findByName("xml"))
                .map(report -> report.getOutputLocation().getAsFile().getOrNull())
                .orElseThrow(() -> new RuntimeException("Could not find junit reports"));
        try {
            return XmlUtils.parseXml(reportHandler, new FileInputStream(sourceReport))
                    .failures();
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(String.format("Failed to parse failures XML: %s", sourceReport), e);
        }
    }

    @Override
    public RuntimeException handleInternalFailure(Path reportDir, RuntimeException ex) {
        Path rawReportsDir = reportDir.resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(rawReportsDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (SingleFileReport rawReport : reporting.getReports()) {
            if (rawReport.getRequired().get()) {
                File rawReportFile = Optional.ofNullable(
                                rawReport.getOutputLocation().getAsFile().getOrNull())
                        .orElseThrow(() -> new IllegalStateException("Could not get raw report file: " + rawReport));
                rawReportFile.renameTo(
                        rawReportsDir.resolve(rawReportFile.getName()).toFile());
            }
        }
        return new RuntimeException(
                "Finalizer failed; raw report files can be found at "
                        + rawReportsDir.getFileName().toString(),
                ex);
    }
}
