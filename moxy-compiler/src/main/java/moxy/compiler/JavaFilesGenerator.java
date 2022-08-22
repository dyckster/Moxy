package moxy.compiler;

import com.squareup.kotlinpoet.FileSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface JavaFilesGenerator<T> {

    @NotNull
    List<FileSpec> generate(T input);
}

