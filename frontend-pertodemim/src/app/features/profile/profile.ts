import { ActivatedRoute } from '@angular/router';
import { AddressFields } from '../../shared/address-fields';
import { accountControls } from '../../shared/account-controls';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Account } from '../../core/models';
@Component({
  standalone: true,
  imports: [ReactiveFormsModule, AddressFields],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private api = inject(ApiService);
  adminId = inject(ActivatedRoute).snapshot.paramMap.get('id');
  endpoint = this.adminId ? 'admin/users/' + this.adminId : 'me';
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  account = signal<Account | null>(null);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');
  form = this.fb.nonNullable.group({
    ...accountControls(),
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
  });
  constructor() {
    this.api
      .get<Account>(this.endpoint)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (user) => {
          this.account.set(user);
          this.form.patchValue(user);
        },
        error: () => this.error.set('Não foi possível carregar seu perfil.'),
      });
  }
  save() {
    this.form.markAllAsTouched();
    this.error.set('');
    this.success.set('');
    const values = this.form.getRawValue();
    if (this.form.invalid || !values.name.trim()) {
      this.error.set('Confira os campos obrigatórios de contato e endereço.');
      return;
    }
    this.saving.set(true);
    this.api
      .put<{ user: Account; token?: string }>(this.endpoint, {
        ...values,
        name: values.name.trim(),
        email: values.email.trim(),
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (result) => {
          this.account.set(result.user);
          if (result.token) this.auth.store(result.token, result.user.role);
          this.success.set('Perfil atualizado com sucesso.');
        },
        error: () =>
          this.error.set(
            'Não foi possível salvar. Confira o e-mail e o endereço ou tente novamente quando o mapa estiver disponível.',
          ),
      });
  }
}
