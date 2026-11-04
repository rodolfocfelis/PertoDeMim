import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: ` <dialog #dialog aria-labelledby="confirmation-title" (cancel)="dismiss($event)">
    <h2 id="confirmation-title">{{ title }}</h2>
    <p>{{ message }}</p>
    <div class="actions">
      <button class="btn-secondary" [disabled]="busy" (click)="cancel.emit()">Cancelar</button>
      <button class="btn-danger" [disabled]="busy" (click)="confirm.emit()">
        {{ busy ? 'Processando...' : 'Confirmar' }}
      </button>
    </div>
  </dialog>`,
})
export class ConfirmDialog implements AfterViewInit {
  @Input() title = 'Confirmar ação';
  @Input() message = '';
  @Input() busy = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  ngAfterViewInit() {
    this.dialog.nativeElement.showModal();
  }
  dismiss(event: Event) {
    event.preventDefault();
    if (!this.busy) this.cancel.emit();
  }
}
