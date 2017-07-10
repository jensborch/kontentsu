import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { TemplateComponent } from './template/template.component';
import { ContentService } from './content/content.service';
import { Logger } from './logger/logger.service';
import { MaterialModule } from '@angular/material';

const routes: Routes = [
  { path: '', component: AppComponent },
  { path: '**', component: AppComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    TemplateComponent
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
    Logger
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
