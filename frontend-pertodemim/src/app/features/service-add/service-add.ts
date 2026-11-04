import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { finalize, timeout } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Category, Professional } from '../../core/models';
import { ConfirmDialog } from '../../shared/confirm-dialog';
@Component({
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmDialog],
  templateUrl: './service-add.html',
  styleUrl: './service-add.scss',
})
export class ServiceAdd {
  private api = inject(ApiService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  categories = signal<Category[]>([]);
  services = signal<Professional[]>([]);
  loading = signal(false);
  saving = signal(false);
  searchingCep = signal(false);
  error = signal('');
  success = signal('');
  editing = signal<number | null>(null);
  deleting = signal<Professional | null>(null);
  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(2000)],
    startingPrice: this.fb.control<number | null>(null, [
      Validators.min(0),
      Validators.max(9999999999.99),
    ]),
    acceptingRequests: [true],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9()+\s-]{8,20}$/)]],
    categoryId: ['', Validators.required],
    cep: [''],
    street: ['', Validators.required],
    addressNumber: ['', Validators.required],
    complement: [''],
    city: ['', Validators.required],
    state: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
  });
  constructor() {
    this.api.get<Category[]>('categories').subscribe({
      next: (data) => this.categories.set(data),
      error: () => this.error.set('Não foi possível carregar as categorias.'),
    });
    this.load();
  }
  load() {
    this.loading.set(true);
    this.api
      .get<Professional[]>('professionals/mine')
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => this.services.set(data),
        error: () => this.error.set('Não foi possível carregar seus serviços.'),
      });
  }
  edit(service: Professional) {
    this.editing.set(service.id);
    this.error.set('');
    this.success.set('');
    this.form.patchValue({
      ...service,
      description: service.description ?? '',
      startingPrice: service.startingPrice ?? null,
      acceptingRequests: service.acceptingRequests ?? true,
      categoryId: String(service.category?.id ?? ''),
      cep: service.zipCode ?? '',
    });
  }
  reset() {
    this.editing.set(null);
    this.form.reset({ acceptingRequests: true });
  }
  buscarCep() {
    const cep = this.form.controls.cep.value.replace(/\D/g, '');
    if (cep.length !== 8) return;
    this.searchingCep.set(true);
    this.error.set('');
    this.http
      .get<{ erro?: boolean; logradouro: string; localidade: string; uf: string }>(
        `https://viacep.com.br/ws/${cep}/json/`,
      )
      .pipe(
        timeout(10000),
        finalize(() => this.searchingCep.set(false)),
      )
      .subscribe({
        next: (address) => {
          if (address.erro) {
            this.error.set('CEP não encontrado.');
            return;
          }
          this.form.patchValue({
            street: address.logradouro,
            city: address.localidade,
            state: address.uf,
          });
        },
        error: () =>
          this.error.set('Consulta de CEP indisponível. Preencha o endereço manualmente.'),
      });
  }
  submitForm() {
    this.form.markAllAsTouched();
    this.error.set('');
    this.success.set('');
    if (this.form.invalid) {
      this.error.set('Confira os campos obrigatórios, telefone e estado.');
      return;
    }
    const values = this.form.getRawValue();
    const body = {
      ...values,
      state: values.state.toUpperCase(),
      zipCode: values.cep.replace(/\D/g, ''),
      category: { id: Number(values.categoryId) },
    };
    const id = this.editing();
    this.saving.set(true);
    const request =
      id === null
        ? this.api.post('professionals', body)
        : this.api.put(`professionals/${id}`, body);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.reset();
        this.success.set('Serviço salvo com sucesso.');
        this.load();
      },
      error: () =>
        this.error.set(
          'Não foi possível salvar. Confira o endereço ou tente novamente quando o serviço de mapas estiver disponível.',
        ),
    });
  }
  remove() {
    const service = this.deleting();
    if (!service) return;
    this.saving.set(true);
    this.error.set('');
    this.success.set('');
    this.api
      .delete(`professionals/${service.id}`)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.deleting.set(null);
          this.success.set('Serviço desativado. Seu histórico foi preservado.');
          this.load();
        },
        error: () => {
          this.deleting.set(null);
          this.error.set('Não foi possível desativar o serviço.');
        },
      });
  }
}
