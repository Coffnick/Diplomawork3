package com.example.diplomawork

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout

import com.example.diplomawork.databinding.ActivityProfileBinding
import com.example.diplomawork.ui.CategoriesFragment
import com.example.diplomawork.ui.MapFragment
import com.example.diplomawork.ui.ReadyRoutesFragment

import com.example.diplomawork.ui.Routes
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.yandex.mapkit.MapKitFactory


class Profile() : AppCompatActivity() {
    private lateinit var bindning: ActivityProfileBinding


    private lateinit var mDrawer: Drawer
    private lateinit var mHeader: AccountHeader
    private lateinit var mToolbar: Toolbar
    private lateinit var mDrawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("c7916f1b-9206-4fa1-92d2-dad1bc862be6")
        MapKitFactory.initialize(applicationContext)

        bindning = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindning.root)


    }


    override fun onStart() {
        super.onStart()
        initFields()
        initFunc()
    }

    private fun initFields() {
        mToolbar = bindning.mainToolBar

    }

    private fun initFunc() {
        setSupportActionBar(mToolbar)
        supportFragmentManager.beginTransaction()
            .replace(R.id.dataContainer, MapFragment()).commit()
        createHeader()
        createDrawer()
        mDrawerLayout = mDrawer.drawerLayout
    }

    private fun createDrawer() {
        mDrawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(mToolbar)
            .withActionBarDrawerToggle(true)
            .withSelectedItem(-1)
            .withAccountHeader(mHeader)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withIdentifier(100)
                    .withIconTintingEnabled(true)
                    .withName("Выбор маршрута")
                    .withSelectable(false),
                PrimaryDrawerItem()
                    .withIdentifier(101)
                    .withIconTintingEnabled(true)
                    .withName("Категории достопримичательности")
                    .withSelectable(false)
            ).withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    when (position) {
                        1 -> supportFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.dataContainer, ReadyRoutesFragment()).commit()

                        2 -> supportFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.dataContainer, CategoriesFragment()).commit()
                    }
                    return false
                }
            }).build()
    }

    private fun createHeader() {
        mHeader = AccountHeaderBuilder().withActivity(this)
            .withHeaderBackground(R.drawable.header)
            .addProfiles(ProfileDrawerItem().withEmail("asdfigf")).build()
    }

    fun disableDrawer() {
        mDrawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mToolbar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    fun enableDrawer() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mDrawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        mToolbar.setNavigationOnClickListener {
            mDrawer.openDrawer()
        }
    }
}