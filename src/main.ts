// import { bootstrapApplication } from '@angular/platform-browser';
// import { App } from './app/app';
// import { provideHttpClient } from '@angular/common/http';
// import { provideRouter } from '@angular/router';   // ✅ ADD THIS
// import { routes } from './app/app.routes';

// bootstrapApplication(App, {
//   providers: [
//     provideHttpClient(),
//     provideRouter(routes)
//   ]
// });


import { bootstrapApplication }
from '@angular/platform-browser';

import { App }
from './app/app';

import { appConfig }
from './app/app.config';

bootstrapApplication(
  App,
  appConfig
)
.catch((err) =>
  console.error(err)
);