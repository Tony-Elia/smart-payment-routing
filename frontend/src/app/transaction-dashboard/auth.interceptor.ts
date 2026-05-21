import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // In a real production app, you would retrieve this token from your AuthService,
  // localStorage, sessionStorage, or an NGRX/Signal store.
  // For demonstration, we'll try to get it from localStorage, falling back to a hardcoded string.
  const token = typeof window !== 'undefined' ? localStorage.getItem('jwt_token')
    : null;

  if (token) {
    // Clone the request and securely attach the Authorization header containing the Bearer token
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });

    // Pass the cloned, authenticated request to the next handler in the chain
    return next(authReq);
  }

  // If no token exists, just pass the unmodified original request along
  return next(req);
};
