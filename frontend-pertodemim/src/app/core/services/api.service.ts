import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { timeout } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  get<T = any>(endpoint: string) {
    return this.http.get<T>(`/api/${endpoint}`).pipe(timeout(20000));
  }
  post<T = any>(endpoint: string, body: unknown) {
    return this.http.post<T>(`/api/${endpoint}`, body).pipe(timeout(20000));
  }
  put<T = any>(endpoint: string, body: unknown) {
    return this.http.put<T>(`/api/${endpoint}`, body).pipe(timeout(20000));
  }
  patch<T = any>(endpoint: string, body: unknown) {
    return this.http.patch<T>(`/api/${endpoint}`, body).pipe(timeout(20000));
  }
  delete(endpoint: string) {
    return this.http.delete<void>(`/api/${endpoint}`).pipe(timeout(20000));
  }
}
