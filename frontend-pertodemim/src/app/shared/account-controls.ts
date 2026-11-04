import { Validators } from '@angular/forms';
export function accountControls() {
  return {
    phone: ['', [Validators.required, Validators.pattern(/^[0-9()+\s-]{10,20}$/)]],
    zipCode: ['', [Validators.required, Validators.pattern(/^[0-9]{5}-?[0-9]{3}$/)]],
    street: ['', [Validators.required, Validators.maxLength(150)]],
    addressNumber: ['', [Validators.required, Validators.maxLength(20)]],
    complement: ['', Validators.maxLength(100)],
    neighborhood: ['', Validators.maxLength(100)],
    city: ['', [Validators.required, Validators.maxLength(100)]],
    state: ['', [Validators.required, Validators.pattern(/^[a-zA-Z]{2}$/)]],
    bio: ['', Validators.maxLength(2000)],
  };
}
