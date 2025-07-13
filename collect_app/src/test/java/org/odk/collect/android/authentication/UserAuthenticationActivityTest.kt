package org.odk.collect.android.authentication

import org.junit.Test
import org.junit.Assert.*
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.android.preferences.keys.ProjectKeys

class UserAuthenticationActivityTest {
    
    @Test
    fun `isUserAuthenticated returns false when not authenticated`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertFalse(result)
    }
    
    @Test
    fun `isUserAuthenticated returns true when authenticated with username`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "testuser")
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertTrue(result)
    }
    
    @Test
    fun `isUserAuthenticated returns true when authenticated with phone`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "123456789")
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertTrue(result)
    }
    
    @Test
    fun `isUserAuthenticated returns true when authenticated with email`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "test@example.com")
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertTrue(result)
    }
    
    @Test
    fun `isUserAuthenticated returns false when authenticated flag is true but no identity fields`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertFalse(result)
    }
    
    @Test
    fun `isUserAuthenticated returns false when authenticated flag is true but empty identity fields`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "")
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertFalse(result)
    }
    
    @Test
    fun `isUserAuthenticated returns false when explicitly set to false`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, false)
        
        val result = UserAuthenticationActivity.isUserAuthenticated(settingsProvider)
        
        assertFalse(result)
    }
    
    @Test
    fun `resetAuthenticationIfEmpty resets authentication when all fields are empty`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "")
        
        UserAuthenticationActivity.resetAuthenticationIfEmpty(settingsProvider)
        
        val result = settingsProvider.getUnprotectedSettings().getBoolean(UserAuthenticationActivity.USER_AUTHENTICATED_KEY)
        assertFalse(result)
    }
    
    @Test
    fun `resetAuthenticationIfEmpty does not reset authentication when at least one field is filled`() {
        val settingsProvider: SettingsProvider = InMemSettingsProvider()
        settingsProvider.getUnprotectedSettings().save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "testuser")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "")
        
        UserAuthenticationActivity.resetAuthenticationIfEmpty(settingsProvider)
        
        val result = settingsProvider.getUnprotectedSettings().getBoolean(UserAuthenticationActivity.USER_AUTHENTICATED_KEY)
        assertTrue(result)
    }
} 