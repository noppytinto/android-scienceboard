package com.nocorp.scienceboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivityViewModel extends AndroidViewModel {
    private Disposable mainActivityContentDisposable;


    public MainActivityViewModel(@NonNull @NotNull Application application) {
        super(application);

    }

    public Disposable getMainActivityContentDisposable() {
        return mainActivityContentDisposable;
    }

    public void setMainActivityContentDisposable(Disposable mainActivityContentDisposable) {
        this.mainActivityContentDisposable = mainActivityContentDisposable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposeDisposable(mainActivityContentDisposable);
    }

    private void disposeDisposable(Disposable disposable) {
        if(disposable!=null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}// end MainActivityViewModel
