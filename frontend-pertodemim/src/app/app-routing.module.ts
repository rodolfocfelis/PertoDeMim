import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
const search = () => import('./features/search/search.component').then((m) => m.SearchComponent);
const profile = () => import('./features/profile/profile').then((m) => m.Profile);
const services = () => import('./features/service-add/service-add').then((m) => m.ServiceAdd);
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin-dashboard/admin-dashboard').then((m) => m.AdminDashboard),
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin-users/admin-users').then((m) => m.AdminUsers),
      },
      { path: 'users/:id', loadComponent: profile },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/admin-categories/admin-categories').then((m) => m.AdminCategories),
      },
      { path: 'search', loadComponent: search },
      { path: 'profile', loadComponent: profile },
      { path: '', redirectTo: 'users', pathMatch: 'full' },
    ],
  },
  {
    path: 'app/user',
    canActivate: [authGuard],
    data: { roles: ['USER'] },
    children: [
      {
        path: 'favorites',
        loadComponent: () => import('./features/favorites/favorites').then((m) => m.Favorites),
      },
      {
        path: 'requests',
        loadComponent: () => import('./features/requests/requests').then((m) => m.Requests),
      },
      { path: 'search', loadComponent: search },
      { path: 'profile', loadComponent: profile },
      { path: '', redirectTo: 'search', pathMatch: 'full' },
    ],
  },
  {
    path: 'app/professional',
    canActivate: [authGuard],
    data: { roles: ['PROFESSIONAL'] },
    children: [
      {
        path: 'requests',
        loadComponent: () => import('./features/requests/requests').then((m) => m.Requests),
      },
      { path: 'search', loadComponent: search },
      { path: 'profile', loadComponent: profile },
      { path: 'services', loadComponent: services },
      { path: '', redirectTo: 'services', pathMatch: 'full' },
    ],
  },
  { path: 'forbidden', loadComponent: () => import('./shared/forbidden').then((m) => m.Forbidden) },
  { path: 'search', redirectTo: '/app/user/search', pathMatch: 'full' },
  { path: 'cadastro', redirectTo: '/app/professional/services', pathMatch: 'full' },
  { path: 'usuarios', redirectTo: '/admin/users', pathMatch: 'full' },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
