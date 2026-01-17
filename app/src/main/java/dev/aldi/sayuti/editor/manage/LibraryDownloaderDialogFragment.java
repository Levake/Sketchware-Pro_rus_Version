package dev.aldi.sayuti.editor.manage;

import static dev.aldi.sayuti.editor.manage.LocalLibrariesUtil.createLibraryMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import org.cosmic.ide.dependency.resolver.api.Artifact;

import java.util.List;
import java.util.concurrent.Executors;

import mod.hey.studios.build.BuildSettings;
import mod.hey.studios.util.Helper;
import mod.jbk.build.BuiltInLibraries;
import mod.pranav.dependency.resolver.DependencyResolver;
import pro.sketchware.R;
import pro.sketchware.databinding.LibraryDownloaderDialogBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

public class LibraryDownloaderDialogFragment extends BottomSheetDialogFragment {
    private LibraryDownloaderDialogBinding binding;

    private final Gson gson = new Gson();
    private BuildSettings buildSettings;

    private boolean notAssociatedWithProject;
    private String dependencyName;
    private String localLibFile;
    private OnLibraryDownloadedTask onLibraryDownloadedTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LibraryDownloaderDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) return;

        notAssociatedWithProject = getArguments().getBoolean("notAssociatedWithProject", false);
        buildSettings = (BuildSettings) getArguments().getSerializable("buildSettings");
        localLibFile = getArguments().getString("localLibFile");

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnDownload.setOnClickListener(v -> initDownloadFlow());
    }

    public void setOnLibraryDownloadedTask(OnLibraryDownloadedTask onLibraryDownloadedTask) {
        this.onLibraryDownloadedTask = onLibraryDownloadedTask;
    }

    private void initDownloadFlow() {
        dependencyName = Helper.getText(binding.dependencyInput);
        if (dependencyName == null || dependencyName.isEmpty()) {
            binding.dependencyInputLayout.setError("Пожалуйста, укажите зависимость");
            binding.dependencyInputLayout.setErrorEnabled(true);
            return;
        }

        var parts = dependencyName.split(":");
        if (parts.length != 3) {
            binding.dependencyInputLayout.setError("Недопустимый формат зависимостей");
            binding.dependencyInputLayout.setErrorEnabled(true);
            return;
        }

        binding.dependencyInfo.setText("В поисках зависимости...");
        binding.dependencyInputLayout.setErrorEnabled(false);
        setDownloadState(true);

        var group = parts[0];
        var artifact = parts[1];
        var version = parts[2];
        var resolver = new DependencyResolver(group, artifact, version, binding.cbSkipSubdependencies.isChecked(), buildSettings);
        var handler = new Handler(Looper.getMainLooper());

        class SetTextRunnable implements Runnable {
            private final String text;

            SetTextRunnable(String text) {
                this.text = text;
            }

            @Override
            public void run() {
                binding.dependencyInfo.setText(text);
            }
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            BuiltInLibraries.maybeExtractAndroidJar((message, progress) -> handler.post(new SetTextRunnable(message)));
            BuiltInLibraries.maybeExtractCoreLambdaStubsJar();

            resolver.resolveDependency(new DependencyResolver.DependencyResolverCallback() {
                @Override
                public void onResolving(@NonNull Artifact artifact, @NonNull Artifact dependency) {
                    handler.post(new SetTextRunnable("Решения " + dependency + " для " + artifact + "..."));
                }

                @Override
                public void onResolutionComplete(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Зависимость " + dep + " решенный"));
                }

                @Override
                public void onArtifactNotFound(@NonNull Artifact dep) {
                    handler.post(() -> {
                        setDownloadState(false);
                        SketchwareUtil.showAnErrorOccurredDialog(getActivity(), "Зависимость '" + dep + "' не найдено");
                    });
                }

                @Override
                public void onSkippingResolution(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Пропуск разрешения для " + dep));
                }

                @Override
                public void onVersionNotFound(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Версия, недоступная для " + dep));
                }

                @Override
                public void onDependenciesNotFound(@NonNull Artifact dep) {
                    handler.post(() -> new SetTextRunnable("Зависимости, не найденные для \"" + dep + "\"").run());
                }

                @Override
                public void onInvalidScope(@NonNull Artifact dep, @NonNull String scope) {
                    handler.post(new SetTextRunnable("Недопустимая область применения для " + dep + ": " + scope));
                }

                @Override
                public void invalidPackaging(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Недопустимая упаковка для зависимостей " + dep));
                }

                @Override
                public void onDownloadStart(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Зависимость от загрузки " + dep + "..."));
                }

                @Override
                public void onDownloadEnd(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Зависимость " + dep + " загруженный"));
                }

                @Override
                public void onDownloadError(@NonNull Artifact dep, @NonNull Throwable e) {
                    handler.post(() -> {
                        setDownloadState(false);
                        SketchwareUtil.showAnErrorOccurredDialog(getActivity(), "Зависимость от загрузки '" + dep + "' неудачно: " + Log.getStackTraceString(e));
                    });
                }

                @Override
                public void unzipping(@NonNull Artifact artifact) {
                    handler.post(new SetTextRunnable("Распаковка зависимости " + artifact));
                }

                @Override
                public void dexing(@NonNull Artifact dep) {
                    handler.post(new SetTextRunnable("Ослабляющая зависимость " + dep));
                }

                @Override
                public void dexingFailed(@NonNull Artifact dependency, @NonNull Exception e) {
                    handler.post(() -> {
                        setDownloadState(false);
                        SketchwareUtil.showAnErrorOccurredDialog(getActivity(), "Ослабляющая зависимость '" + dependency + "' неудачно: " + Log.getStackTraceString(e));
                    });
                }

                @Override
                public void onTaskCompleted(@NonNull List<String> dependencies) {
                    handler.post(() -> {
                        SketchwareUtil.toast("Библиотека успешно загружена");
                        if (!notAssociatedWithProject) {
                            new SetTextRunnable("Добавление зависимостей в проект...").run();
                            var fileContent = FileUtil.readFile(localLibFile);
                            var enabledLibs = gson.fromJson(fileContent, Helper.TYPE_MAP_LIST);
                            enabledLibs.addAll(dependencies.stream()
                                    .map(name -> createLibraryMap(name, dependencyName))
                                    .toList());
                            FileUtil.writeFile(localLibFile, gson.toJson(enabledLibs));
                        }
                        if (getActivity() == null) return;
                        dismiss();
                        if (onLibraryDownloadedTask != null) onLibraryDownloadedTask.invoke();
                    });
                }
            });
        });
    }

    private void setDownloadState(boolean downloading) {
        binding.btnCancel.setVisibility(downloading ? View.GONE : View.VISIBLE);
        binding.btnDownload.setEnabled(!downloading);
        binding.dependencyInput.setEnabled(!downloading);
        binding.cbSkipSubdependencies.setEnabled(!downloading);
        setCancelable(!downloading);

        if (!downloading) {
            binding.dependencyInfo.setText(R.string.local_library_manager_dependency_info);
        }
    }

    public interface OnLibraryDownloadedTask {
        void invoke();
    }
}
