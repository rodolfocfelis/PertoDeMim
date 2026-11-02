import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Category, Page } from '../../core/models';
import { ConfirmDialog } from '../../shared/confirm-dialog';
@Component({
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmDialog],
  templateUrl: './admin-categories.html',
})
export class AdminCategories {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  categories = signal<Category[]>([]);
  page = signal(0);
  pages = signal(0);
  loading = signal(false);
  saving = signal(false);
  error = signal('');
  success = signal('');
  editing = signal<number | null>(null);
  deleting = signal<Category | null>(null);
  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
  });
  constructor() {
    this.load();
  }
  load(page = 0) {
    this.loading.set(true);
    this.api
      .get<Page<Category>>(`admin/categories?page=${page}&size=10`)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => {
          this.categories.set(result.content);
          this.page.set(result.number);
          this.pages.set(result.totalPages);
        },
        error: () => this.error.set('Não foi possível carregar as categorias.'),
      });
  }
  edit(category: Category) {
    this.editing.set(category.id);
    this.form.patchValue(category);
    this.success.set('');
  }
  reset() {
    this.editing.set(null);
    this.form.reset();
  }
  save() {
    this.error.set('');
    this.success.set('');
    this.form.markAllAsTouched();
    const values = this.form.getRawValue();
    if (this.form.invalid || !values.name.trim()) {
      this.error.set('Informe o nome da categoria.');
      return;
    }
    const body = { ...values, name: values.name.trim() };
    const id = this.editing();
    this.saving.set(true);
    const request =
      id === null
        ? this.api.post('admin/categories', body)
        : this.api.put(`admin/categories/${id}`, body);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.reset();
        this.success.set('Categoria salva.');
        this.load();
      },
      error: () => this.error.set('Não foi possível salvar a categoria.'),
    });
  }
  remove() {
    const category = this.deleting();
    if (!category) return;
    this.saving.set(true);
    this.error.set('');
    this.success.set('');
    this.api
      .delete(`admin/categories/${category.id}`)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.deleting.set(null);
          this.success.set('Categoria excluída.');
          this.load();
        },
        error: () => {
          this.deleting.set(null);
          this.error.set('Categoria em uso ou indisponível para exclusão.');
        },
      });
  }
}
