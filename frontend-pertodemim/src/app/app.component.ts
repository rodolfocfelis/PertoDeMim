import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class App {
  auth = inject(AuthService);
  scope(): string {
    if (this.auth.hasRole('ADMIN')) return '/admin';
    return this.auth.hasRole('PROFESSIONAL') ? '/app/professional' : '/app/user';
  }
}
