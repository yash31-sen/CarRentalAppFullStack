import {
  HttpInterceptorFn
} from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const token = localStorage.getItem('token');

  console.log('Interceptor token:', token);

  if (token) {

    req = req.clone({
      setHeaders: {
        Authorization: token
      }
    });
  }

  return next(req);
};