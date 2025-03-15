import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {ProfileComponent} from './profile.component';
import {OverviewComponent} from './overview/overview.component';
import {SavePwComponent} from './save-pw/save-pw.component';
import {ConfirmationComponent} from './confirmation/confirmation.component';
import {ProfileNavComponent} from './profile-nav/profile-nav.component';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {EditorComponent} from './editor/editor.component';

const routes: Routes = [
  { path: '', component: ProfileComponent, children: [
      {path:'profile', redirectTo: 'profile/(sub:overview)', pathMatch: 'full'},
      {path: 'overview', component: OverviewComponent, outlet: 'sub'},
      {path: 'change-password', component: SavePwComponent, outlet: 'sub'},
      {path: 'editor', component: EditorComponent, outlet: 'sub'}
    ]},
]

@NgModule({
  declarations: [
    ProfileComponent,
    ConfirmationComponent,
    OverviewComponent,
    ProfileNavComponent,
    SavePwComponent,
    EditorComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule
  ]
})
export class ProfileModule { }
