import { Injectable } from '@angular/core';
import { finalize, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DataRefreshService {
  execute<T>(
    requestFactory: () => Observable<T>,
    config: {
      setLoading: (value: boolean) => void;
      reset?: () => void;
      onSuccess?: (data: T) => void;
      onError?: (error: unknown) => void;
    },
  ): void {
    config.reset?.();
    config.setLoading(true);

    requestFactory()
      .pipe(finalize(() => config.setLoading(false)))
      .subscribe({
        next: (data) => config.onSuccess?.(data),
        error: (error) => config.onError?.(error),
      });
  }
}
