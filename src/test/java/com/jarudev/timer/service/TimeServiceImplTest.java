package com.jarudev.timer.service;

import com.jarudev.timer.exception.InternalExceptionHandler;
import com.jarudev.timer.repository.TimeRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeServiceImplTest {

    @Mock
    private TimeRepositoryImpl repository;

    @Mock
    private InternalExceptionHandler handler;

    @InjectMocks
    private TimeServiceImpl service;

    @Test
    void writeOne_whenSuccess_shouldCallRepositoryAndNotHandler() {
        LocalDateTime dt = LocalDateTime.now();

        service.writeOne(dt);

        verify(repository).saveOne(dt);
        verifyNoInteractions(handler);
    }

    @Test
    void writeOne_whenDataAccessException_shouldDelegateToHandler() {
        LocalDateTime dt = LocalDateTime.now();
        DataAccessException dae =
                new DataAccessResourceFailureException("DB error", new RuntimeException());

        doThrow(dae).when(repository).saveOne(dt);

        service.writeOne(dt);

        ArgumentCaptor<List<LocalDateTime>> listCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<DataAccessException> exCaptor = ArgumentCaptor.forClass(DataAccessException.class);

        verify(handler).handleWriteFailure(listCaptor.capture(), exCaptor.capture());

        assertThat(listCaptor.getValue()).containsExactly(dt);
        assertThat(exCaptor.getValue()).isSameAs(dae);
    }

    @Test
    void writeBatch_whenEmptyList_shouldDoNothing() {
        service.writeBatch(List.of());

        verifyNoInteractions(repository);
        verifyNoInteractions(handler);
    }

    @Test
    void writeBatch_whenSuccess_shouldCallRepositoryAndNotHandler() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        List<LocalDateTime> batch = List.of(t1, t2);

        service.writeBatch(batch);

        verify(repository).saveBatch(batch);
        verifyNoInteractions(handler);
    }

    @Test
    void writeBatch_whenDataAccessException_shouldDelegateToHandler() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        List<LocalDateTime> batch = List.of(t1, t2);

        DataAccessException dae =
                new DataAccessResourceFailureException("DB error", new RuntimeException());
        doThrow(dae).when(repository).saveBatch(batch);

        service.writeBatch(batch);

        verify(handler).handleWriteFailure(batch, dae);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        List<LocalDateTime> expected = List.of(t1, t2);

        when(repository.findAll()).thenReturn(expected);

        List<LocalDateTime> actual = service.findAll();

        assertThat(actual).isEqualTo(expected);
        verify(repository).findAll();
        verifyNoInteractions(handler);
    }
}