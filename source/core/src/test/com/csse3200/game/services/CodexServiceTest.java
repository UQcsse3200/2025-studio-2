package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class CodexServiceTest {
    private void setupMockFile(String content) {
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        FileHandle fileHandle = mock(FileHandle.class);

        // Reading string from file handle returns 'content' instead
        when(fileHandle.readString()).thenReturn(content);
        // Reading a file returns mocked file handle
        when(Gdx.files.internal("codex-entries.txt")).thenReturn(fileHandle);
    }
   }
}
