import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { TemplateComponent } from './template/template.component';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { MaterialModule } from '@angular/material';
import { NotfoundComponent } from './notfound/notfound.component';

const routes: Routes = [
  { path: '', component: AppComponent },
  { path: 'pages/page1', component: AppComponent },
  { path: 'pages/page2', component: AppComponent },
  { path: '**', component: NotfoundComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    TemplateComponent,
    NotfoundComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    MaterialModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    ContentService,
    Logger,
    Location,
    { provide: LocationStrategy, useClass: PathLocationStrategy }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
